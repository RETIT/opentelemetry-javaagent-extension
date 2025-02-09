package io.retit.opentelemetry.javaagent.extension.frameworks;

import io.restassured.RestAssured;
import io.retit.opentelemetry.javaagent.extension.common.ContainerLogMetricAndSpanExtractingTest;
import io.retit.opentelemetry.javaagent.extension.common.SpanDemand;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

public class AbstractFrameworkIT extends ContainerLogMetricAndSpanExtractingTest {

    private static final String CONTEXT_ROOT = "/test-rest-endpoint";

    private static final String GET_URI = CONTEXT_ROOT + "/getData";

    private static final String POST_URI = CONTEXT_ROOT + "/postData";

    private static final String DELETE_URI = CONTEXT_ROOT + "/deleteData";

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
        } else {
            applicationContainer = applicationContainer
                    .withEnv("QUARKUS_OTEL_ENABLED", "true") // enable quarkus internal OpenTelemetry Support
                    .withEnv("QUARKUS_OTEL_SIMPLE", "true") // send telemetry right away
                    .withEnv("JAVA_TOOL_OPTIONS", "-Dotel.javaagent.extensions=/otel/io.retit.opentelemetry.javaagent.extension.jar");
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
            if(spanName.contains(CONTEXT_ROOT)) {
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
