package io.retit.opentelemetry.javaagent.extension;

import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


class JavaAgentExtensionIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaAgentExtensionIntegrationTest.class);
    private static GenericContainer<?> applicationContainer;
    private static Map<String, List<SpanDemand>> spanDemands;
    private static List<MetricDemand> metricDemands;

    // Operations in SampleApplication
    private static final String SAMPLE_METHOD = "SampleApplication.method";
    private static final String SAMPLE_METHOD1 = "SampleApplication.method1";
    private static final String SAMPLE_METHOD2 = "SampleApplication.method2";

    // Metrics to be tested

    private static final String[] METRIC_NAMES = {Constants.SPAN_ATTRIBUTE_PROCESS_CPU_TIME, "io.retit.emissions.cpu.power.min", "io.retit.emissions.cpu.power.max",
            "io.retit.emissions.embodied.emissions.minute.mg", "io.retit.emissions.memory.energy.gb.minute",
            "io.retit.emissions.storage.energy.gb.minute", "io.retit.emissions.network.energy.gb.minute",
            "io.retit.emissions.pue", "io.retit.emissions.gef", "io.retit.resource.demand.storage.bytes",
            "io.retit.resource.demand.memory.bytes", "io.retit.resource.demand.network.bytes",
            "io.retit.resource.demand.cpu.ms"
    };

    @BeforeEach
    public void setupApplication() {
        String image = "otel-integration-test:feature";
        LOGGER.info("Using image: " + image);
        applicationContainer = new GenericContainer<>(image)
                .withEnv("OTEL_LOGS_EXPORTER", "none")
                .withEnv("OTEL_METRICS_EXPORTER", "logging")
                .withEnv("OTEL_TRACES_EXPORTER", "logging")
                .withEnv("JAVA_TOOL_OPTIONS", "-javaagent:opentelemetry-javaagent-all.jar -Dotel.javaagent.extensions=io.retit.opentelemetry.javaagent.extension.jar");
        spanDemands = new HashMap<>();
        metricDemands = new ArrayList<>();
    }

    @AfterEach
    public void removeApplication() {
        LOGGER.info(applicationContainer.getLogs());
        applicationContainer.stop();
        LOGGER.info(spanDemands.toString());
    }

    private void executeContainer() {
        // Start container and attach parser to log output
        applicationContainer.start();
        applicationContainer.followOutput(outputFrame -> {
            String logOutput = outputFrame.getUtf8String();
            addToSpanDemands(logOutput);
            addToMetricDemands(logOutput);
        });

        // Wait until container has stopped running
        while (applicationContainer.isRunning()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("Interupted", e);
            }
        }
    }

    /**
     * Test that all @WithSpan operations are traced + TODO: correct java_agent version is used
     *
     * @throws InterruptedException
     */
    @Test
    void testAllOperationsPresent() {
        applicationContainer
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");
        executeContainer();

        // Check that operations are there
        Map<String, List<SpanDemand>> methodSpans = spanDemands.entrySet().stream().filter(
                        e -> SAMPLE_METHOD1.equals(e.getKey()) || SAMPLE_METHOD2.equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(2, methodSpans.size());
        assertTrue(methodSpans.containsKey(SAMPLE_METHOD1));
        assertEquals(1, methodSpans.get(SAMPLE_METHOD1).size());
        assertTrue(methodSpans.containsKey(SAMPLE_METHOD2));
        assertEquals(1, methodSpans.get(SAMPLE_METHOD2).size());

        // Check that method1 is traced before method2
        assertTrue(spanDemands.get(SAMPLE_METHOD1).get(0).logSystemTime <= spanDemands.get(SAMPLE_METHOD2).get(0).logSystemTime);

        // GC events are recorded by default. Assert that any other spans belog to GC events
        Map<String, List<SpanDemand>> gcSpans = spanDemands.entrySet().stream().filter(
                        e -> !SAMPLE_METHOD1.equals(e.getKey()) && !SAMPLE_METHOD2.equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertTrue(gcSpans.keySet().stream()
                .allMatch(s -> isGcSpanName(s)));
        LOGGER.info("Found {} gc spans", gcSpans.values().stream().mapToInt(List::size).sum());
    }

    @NotNull
    private static boolean isGcSpanName(String s) {
        return Constants.JAVA_AGENT_GC_OPERATION_NAME_MAJOR_FREE.equals(s) || Constants.JAVA_AGENT_GC_OPERATION_NAME_MINOR_FREE.equals(s);
    }

    @Test
    void testAllAttributes() {
        applicationContainer.withEnv("IO_RETIT_LOG_CPU_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "true")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS")
                .withEnv("WAIT_FOR_ONE_MINUTE", "true"); // we need to wait for one minute for the metrics to be published
        executeContainer();

        Assertions.assertTrue(!spanDemands.isEmpty());

        for (Map.Entry<String, List<SpanDemand>> spanDemandEntryList : spanDemands.entrySet()) {
            if (!isGcSpanName(spanDemandEntryList.getKey())) {
                assertEquals(1, spanDemandEntryList.getValue().size());
            }
            for (SpanDemand spanDemandEntry : spanDemandEntryList.getValue()) {
                assertNotEquals(0, spanDemandEntry.startCpuTime);
                assertNotEquals(0, spanDemandEntry.endCpuTime);
                assertNotEquals(0, spanDemandEntry.startHeapDemand);
                assertNotEquals(0, spanDemandEntry.endHeapDemand);
                if (spanDemandEntryList.getKey().contains(SAMPLE_METHOD)) {
                    assertNull(spanDemandEntry.totalHeapSize);
                } else {
                    assertNotEquals(0, spanDemandEntry.totalHeapSize);
                }
                assertNotEquals(0, spanDemandEntry.startDiskReadDemand);
                assertNotEquals(0, spanDemandEntry.endDiskReadDemand);
                if (!isGcSpanName(spanDemandEntryList.getKey())) {
                    assertNotEquals(0, spanDemandEntry.startDiskWriteDemand);
                    assertNotEquals(0, spanDemandEntry.endDiskWriteDemand);
                }
                assertNotEquals(0, spanDemandEntry.logSystemTime);

                assertNotNull(spanDemandEntry.startNetworkReadDemand);
                assertNotNull(spanDemandEntry.endNetworkReadDemand);
                assertNotNull(spanDemandEntry.startNetworkWriteDemand);
                assertNotNull(spanDemandEntry.endNetworkWriteDemand);
            }
        }

        Assertions.assertTrue(!metricDemands.isEmpty());

        for (String metricName : METRIC_NAMES) {
            LOGGER.info("Asserting metric {}", metricName);
            Optional<MetricDemand> metricDemandResult = metricDemands.stream().filter(metricDemand -> metricName.equals(metricDemand.metricName)).findFirst();
            LOGGER.info("Found metric {} with value {}", metricName, metricDemandResult.get().metricValue);
            Assertions.assertTrue(metricDemandResult.isPresent());
            Assertions.assertNotNull(metricDemandResult.get().metricValue);
            if (!"io.retit.resource.demand.network.bytes".equals(metricDemandResult.get().metricName)) {

                Assertions.assertNotEquals(0.0, metricDemandResult.get().metricValue);
            } else {
                // no network demand
                Assertions.assertEquals(0.0, metricDemandResult.get().metricValue);
            }

        }
    }


    @Test
    void testOnlyCPUDemand() {
        applicationContainer.withEnv("IO_RETIT_LOG_CPU_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "false")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (List<SpanDemand> sds : spanDemands.values()) {
            assertEquals(1, sds.size());
            SpanDemand sd = sds.get(0);
            assertNotEquals(0, sd.startCpuTime);
            assertNotEquals(0, sd.endCpuTime);
            assertNull(sd.startHeapDemand);
            assertNull(sd.endHeapDemand);
            assertNull(sd.totalHeapSize);
            assertNull(sd.startDiskReadDemand);
            assertNull(sd.endDiskReadDemand);
            assertNull(sd.startDiskWriteDemand);
            assertNull(sd.endDiskWriteDemand);
            assertNull(sd.startNetworkReadDemand);
            assertNull(sd.endNetworkReadDemand);
            assertNull(sd.startNetworkWriteDemand);
            assertNull(sd.endNetworkWriteDemand);
        }
    }

    @Test
    void testOnlyLogSystem() {
        applicationContainer.withEnv("IO_RETIT_LOG_CPU_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "false")
                .withEnv("IO_RETIT_LOG_RESPONSE_TIME", "true")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (List<SpanDemand> sds : spanDemands.values()) {
            assertEquals(1, sds.size());
            SpanDemand sd = sds.get(0);
            assertNull(sd.startCpuTime);
            assertNull(sd.endCpuTime);
            // Don't work on Windows
            //assertNotEquals(sd.startSystemTime, 0);
            //assertNotEquals(sd.endSystemTime, 0);
            assertNull(sd.startHeapDemand);
            assertNull(sd.endHeapDemand);
            assertNull(sd.totalHeapSize);
            assertNull(sd.startDiskReadDemand);
            assertNull(sd.endDiskReadDemand);
            assertNull(sd.startDiskWriteDemand);
            assertNull(sd.endDiskWriteDemand);
            assertNull(sd.startNetworkReadDemand);
            assertNull(sd.endNetworkReadDemand);
            assertNull(sd.startNetworkWriteDemand);
            assertNull(sd.endNetworkWriteDemand);
        }
    }

    @Test
    void testOnlyHeapDemand() {
        applicationContainer.withEnv("IO_RETIT_LOG_CPU_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "false")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (List<SpanDemand> sds : spanDemands.values()) {
            assertEquals(1, sds.size());
            SpanDemand sd = sds.get(0);
            assertNull(sd.startCpuTime);
            assertNull(sd.endCpuTime);
            assertNotNull(sd.startHeapDemand);
            assertNotNull(sd.endHeapDemand);
            assertNotEquals(0, sd.startHeapDemand);
            assertNotEquals(0, sd.endHeapDemand);
            assertNull(sd.totalHeapSize);
            assertNull(sd.startDiskReadDemand);
            assertNull(sd.endDiskReadDemand);
            assertNull(sd.startDiskWriteDemand);
            assertNull(sd.endDiskWriteDemand);
            assertNull(sd.startNetworkReadDemand);
            assertNull(sd.endNetworkReadDemand);
            assertNull(sd.startNetworkWriteDemand);
            assertNull(sd.endNetworkWriteDemand);
        }
    }

    @Test
    void testOnlyGCDemands() {
        applicationContainer.withEnv("IO_RETIT_LOG_CPU_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "true")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (Map.Entry<String, List<SpanDemand>> spanDemandEntryList : spanDemands.entrySet()) {
            List<SpanDemand> value = spanDemandEntryList.getValue();
            for (SpanDemand spanDemandEntry : value) {
                assertNull(spanDemandEntry.startCpuTime);
                assertNull(spanDemandEntry.endCpuTime);
                if (spanDemandEntryList.getKey().contains(SAMPLE_METHOD)) {
                    assertNull(spanDemandEntry.startHeapDemand);
                    assertNull(spanDemandEntry.endHeapDemand);
                    assertNull(spanDemandEntry.totalHeapSize);
                } else {
                    assertNotNull(spanDemandEntry.startHeapDemand);
                    assertNotNull(spanDemandEntry.endHeapDemand);
                    assertNotNull(spanDemandEntry.totalHeapSize);
                    assertNotEquals(0, spanDemandEntry.startHeapDemand);
                    assertNotEquals(0, spanDemandEntry.endHeapDemand);
                    assertNotEquals(0, spanDemandEntry.totalHeapSize);
                }
                assertNull(spanDemandEntry.startDiskReadDemand);
                assertNull(spanDemandEntry.endDiskReadDemand);
                assertNull(spanDemandEntry.startDiskWriteDemand);
                assertNull(spanDemandEntry.endDiskWriteDemand);
                assertNull(spanDemandEntry.startNetworkReadDemand);
                assertNull(spanDemandEntry.endNetworkReadDemand);
                assertNull(spanDemandEntry.startNetworkWriteDemand);
                assertNull(spanDemandEntry.endNetworkWriteDemand);
            }
        }
    }

    @Test
    void testOnlyDiskDemand() {
        applicationContainer.withEnv("IO_RETIT_LOG_CPU_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "false")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (List<SpanDemand> sds : spanDemands.values()) {
            assertEquals(1, sds.size());
            SpanDemand sd = sds.get(0);
            assertNull(sd.startCpuTime);
            assertNull(sd.endCpuTime);
            assertNull(sd.startHeapDemand);
            assertNull(sd.endHeapDemand);
            assertNull(sd.totalHeapSize);
            assertNotNull(sd.startDiskReadDemand);
            assertNotNull(sd.endDiskReadDemand);
            assertNotNull(sd.startDiskWriteDemand);
            assertNotNull(sd.endDiskWriteDemand);
            assertNotEquals(0, sd.startDiskReadDemand);
            assertNotEquals(0, sd.endDiskReadDemand);
            assertNotEquals(0, sd.startDiskWriteDemand);
            assertNotEquals(0, sd.endDiskWriteDemand);
            assertNull(sd.startNetworkReadDemand);
            assertNull(sd.endNetworkReadDemand);
            assertNull(sd.startNetworkWriteDemand);
            assertNull(sd.endNetworkWriteDemand);
        }
    }

    @Test
    void testOnlyNetworkDemand() {
        applicationContainer.withEnv("IO_RETIT_LOG_CPU_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_DISK_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_HEAP_DEMAND", "false")
                .withEnv("IO_RETIT_LOG_NETWORK_DEMAND", "true")
                .withEnv("IO_RETIT_LOG_GC_EVENT", "false")
                .withEnv("IO_RETIT_EMISSIONS_STORAGE_TYPE", "SSD")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION", "af-south-1")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE", "a1.medium")
                .withEnv("IO_RETIT_EMISSIONS_CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (List<SpanDemand> sds : spanDemands.values()) {
            assertEquals(1, sds.size());
            SpanDemand sd = sds.get(0);
            assertNull(sd.startCpuTime);
            assertNull(sd.endCpuTime);
            assertNull(sd.startHeapDemand);
            assertNull(sd.endHeapDemand);
            assertNull(sd.totalHeapSize);
            assertNull(sd.startDiskReadDemand);
            assertNull(sd.endDiskReadDemand);
            assertNull(sd.startDiskWriteDemand);
            assertNull(sd.endDiskWriteDemand);
            assertNotNull(sd.startNetworkReadDemand);
            assertNotNull(sd.endNetworkReadDemand);
            assertNotNull(sd.startNetworkWriteDemand);
            assertNotNull(sd.endNetworkWriteDemand);
        }
    }

    private static void addToSpanDemands(String logOutput) {
        // in case of span
        if (logOutput.contains("io.opentelemetry.exporter.logging.LoggingSpanExporter")) {
            String operationName = extractOperationNameFromLogOutput(logOutput);
            SpanDemand spanDemand = parseProperties(logOutput);
            List<SpanDemand> opSpanDemands;
            if (spanDemands.containsKey(operationName)) {
                opSpanDemands = spanDemands.get(operationName);
            } else {
                opSpanDemands = new ArrayList<>();
                spanDemands.put(operationName, opSpanDemands);
            }
            opSpanDemands.add(spanDemand);
        }
    }

    private static void addToMetricDemands(String logOutput) {
        if (logOutput.contains("io.opentelemetry.exporter.logging.LoggingMetricExporter")) {
            LOGGER.info("Processing metric " + logOutput);
            List<MetricDemand> demands = extractMetricValuesFromLog(logOutput);
            if (demands != null) {
                metricDemands.addAll(demands);
            }
        }
    }

    private static List<MetricDemand> extractMetricValuesFromLog(String logMessage) {

        List<MetricDemand> demands = new ArrayList<>();
        String valueAttributeInLog = "value=";

        String[] seperateMetrics = logMessage.split("ImmutableLongPointData");
        for (String key : METRIC_NAMES) {
            for (String metricData : seperateMetrics) {
                if (metricData.contains(key) && metricData.indexOf(valueAttributeInLog) != -1) {

                    MetricDemand metricDemand = new MetricDemand();
                    int valueIndex = metricData.indexOf(valueAttributeInLog);

                    String valueString = metricData.substring(valueIndex + valueAttributeInLog.length(), metricData.indexOf(",", valueIndex));
                    double value = Double.parseDouble(valueString);

                    metricDemand.metricName = key;
                    metricDemand.metricValue = value;
                    demands.add(metricDemand);
                }
            }

        }
        return demands;
    }

    private static String extractOperationNameFromLogOutput(String logOutput) {
        LOGGER.info("Processing span " + logOutput);
        return logOutput.substring(logOutput.indexOf('\'') + 1, logOutput.lastIndexOf('\''));
    }

    private static SpanDemand parseProperties(String logOutput) {
        SpanDemand spanDemand = new SpanDemand();
        String[] properties = extractPropertiesFromLogOutput(logOutput);
        for (String prop : properties) {
            String[] elems = prop.split("=");
            if (elems[0].contains("io.retit.endcputime")) {
                spanDemand.endCpuTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.enddiskreaddemand")) {
                spanDemand.endDiskReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.enddiskwritedemand")) {
                spanDemand.endDiskWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endnetworkreaddemand")) {
                spanDemand.endNetworkReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endnetworkwritedemand")) {
                spanDemand.endNetworkWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endheapbyteallocation")) {
                spanDemand.endHeapDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endsystemtime")) {
                spanDemand.endSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startcputime")) {
                spanDemand.startCpuTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startdiskreaddemand")) {
                spanDemand.startDiskReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startdiskwritedemand")) {
                spanDemand.startDiskWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startnetworkreaddemand")) {
                spanDemand.startNetworkReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startnetworkwritedemand")) {
                spanDemand.startNetworkWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startheapbyteallocation")) {
                spanDemand.startHeapDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startsystemtime")) {
                spanDemand.startSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.logsystemtime")) {
                spanDemand.logSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.totalheapsize")) {
                spanDemand.totalHeapSize = Long.valueOf(elems[1]);
            }
        }
        return spanDemand;
    }

    private static String[] extractPropertiesFromLogOutput(String logOutput) {
        return logOutput.substring(logOutput.indexOf("={") + 1, logOutput.lastIndexOf("},")).split(",");
    }

    private static class SpanDemand {
        public Long startCpuTime = null;
        public Long endCpuTime = null;
        public Long startSystemTime = null;
        public Long endSystemTime = null;
        public Long startHeapDemand = null;
        public Long endHeapDemand = null;
        public Long startDiskReadDemand = null;
        public Long endDiskReadDemand = null;
        public Long startDiskWriteDemand = null;
        public Long endDiskWriteDemand = null;
        public Long startNetworkReadDemand = null;
        public Long endNetworkReadDemand = null;
        public Long startNetworkWriteDemand = null;
        public Long endNetworkWriteDemand = null;
        public Long logSystemTime = null;
        public Long totalHeapSize = null;
    }

    private static class MetricDemand {
        public String metricName;
        public Double metricValue;
    }
}
