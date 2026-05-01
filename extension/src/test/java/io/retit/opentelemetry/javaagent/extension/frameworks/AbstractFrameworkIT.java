package io.retit.opentelemetry.javaagent.extension.frameworks;

import io.restassured.RestAssured;
import io.retit.opentelemetry.javaagent.extension.common.ContainerLogMetricAndSpanExtractingTest;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.GenericContainer;

public class AbstractFrameworkIT extends ContainerLogMetricAndSpanExtractingTest {

    private static final String CONTEXT_ROOT = "/test-rest-endpoint";

    private static final String GET_URI = CONTEXT_ROOT + "/getData";

    private static final String POST_URI = CONTEXT_ROOT + "/postData";

    private static final String DELETE_URI = CONTEXT_ROOT + "/deleteData";

    /**
     * Sets up the container for the library-based Quarkus example ({@code quarkus-rest-service-library}).
     *
     * <p>In this mode no Java-agent is used.  The RETIT {@code RETITSpanProcessor} is registered
     * via CDI and Quarkus' own OpenTelemetry extension manages the SDK lifecycle.  Both traces
     * and metrics are exported to the console so that the log parser can verify them.</p>
     *
     * @param containerName Docker image name (e.g. {@code quarkus-rest-service-library:feature}).
     * @param serviceName   OTel service.name resource attribute.
     * @param portToExpose  HTTP port exposed by the container.
     */
    protected void commonSetupForLibrary(final String containerName, final String serviceName, final int portToExpose) {
        applicationContainer = new GenericContainer<>(containerName)
                .withExposedPorts(portToExpose)
                .withEnv("IO_RETIT_LOG_CPU_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "true")
                .withEnv("OTEL_LOGS_EXPORTER", "none")
                .withEnv("OTEL_TRACES_EXPORTER", "console")
                .withEnv("OTEL_METRICS_EXPORTER", "console")
                .withEnv("OTEL_RESOURCE_ATTRIBUTES", "service.name=" + serviceName)
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS")
                // Quarkus' own OpenTelemetry extension is active – no Java-agent required.
                .withEnv("QUARKUS_OTEL_ENABLED", "true")
                .withEnv("QUARKUS_OTEL_SIMPLE", "true");

        executeContainer(portToExpose);
    }

    protected void commonSetup(final String containerName, final String serviceName, final int portToExpose, final boolean useExternalOtelAgent, final boolean exportMetricsToCollector) {
        applicationContainer = new GenericContainer<>(containerName)
                .withExposedPorts(portToExpose)
                .withEnv("IO_RETIT_LOG_CPU_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "true")
                .withEnv("OTEL_LOGS_EXPORTER", "none")
                .withEnv("OTEL_TRACES_EXPORTER", "console")
                .withEnv("OTEL_RESOURCE_ATTRIBUTES", "service.name=" + serviceName)
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");

        if (exportMetricsToCollector) {
            applicationContainer = applicationContainer.withEnv("OTEL_METRICS_EXPORTER", "otlp").withEnv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://host.docker.internal:4318");
        } else {
            applicationContainer = applicationContainer.withEnv("OTEL_METRICS_EXPORTER", "console");
        }
        if (useExternalOtelAgent) {
            applicationContainer = applicationContainer
                    .withEnv("QUARKUS_OTEL_ENABLED", "false") // disable quarkus internal OpenTelemetry Support as we are using the agent
                    .withEnv("JAVA_TOOL_OPTIONS", "-javaagent:/otel/opentelemetry-javaagent.jar -Dotel.javaagent.extensions=/otel/io.retit.opentelemetry.javaagent.extension.jar");
        }
        executeContainer(portToExpose);
    }

    protected void runTestContinuously() {
        while (true) {
            callAllEndpoints();
        }
    }

    protected void testCallEachEndpointAndAsserSpansAndMetrics() {
        callAllEndpoints();
        applicationContainer.stop();
        super.waitUntilContainerIsStopped();
        int testRestEndpointCount = 0;
        for (String spanName : spanDemands.keySet()) {
            System.out.println(spanName);
            if (spanName.contains(CONTEXT_ROOT)) {
                testRestEndpointCount++;
            }
        }
        Assertions.assertEquals(3, testRestEndpointCount);
        assertFullSpanDataContent(CONTEXT_ROOT);
    }

    protected void callAllEndpoints() {
        callAndAssertGetEndpoint();
        callAndAssertPostEndpoint();
        callAndAssertDeleteEndpoint();
    }

    protected void callAndAssertGetEndpoint() {
        RestAssured.given().get(CONTAINER_URL + GET_URI).then().statusCode(200);
    }

    protected void callAndAssertPostEndpoint() {
        RestAssured.given().post(CONTAINER_URL + POST_URI).then().statusCode(200);
    }

    protected void callAndAssertDeleteEndpoint() {
        RestAssured.given().delete(CONTAINER_URL + DELETE_URI).then().statusCode(200);
    }
}
