package io.retit.opentelemetry.javaagent.extension.frameworks.quarkus;

import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

public class AbstractQuarkusIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQuarkusIT.class);

    protected static String QUARKUS_CONTAINER_URL = "http://localhost:";

    protected static GenericContainer<?> applicationContainer;

    @AfterEach
    public void removeApplication() {
        LOGGER.info(applicationContainer.getLogs());
        applicationContainer.stop();
    }

    protected void commonSetup(final boolean useExternalOtelAgent, final boolean exportMetricsToCollector) {
        applicationContainer = new GenericContainer<>("quarkus-rest-service:feature")
                .withExposedPorts(8080)
                .withEnv("OTEL_LOGS_EXPORTER", "none")
                .withEnv("OTEL_TRACES_EXPORTER", "console")
                .withEnv("OTEL_RESOURCE_ATTRIBUTES", "service.name=quarkus-app")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");

        if(exportMetricsToCollector) {
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
        executeContainer();
    }

    private void executeContainer() {
        // Start container and attach parser to log output
        applicationContainer.start();
        QUARKUS_CONTAINER_URL = QUARKUS_CONTAINER_URL + applicationContainer.getMappedPort(8080);
        applicationContainer.followOutput(outputFrame -> {
            String logOutput = outputFrame.getUtf8String();
//            addToSpanDemands(logOutput);
//            addToMetricDemands(logOutput);
        });
    }
}
