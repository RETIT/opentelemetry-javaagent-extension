package io.retit.opentelemetry.javaagent.extension;

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
        applicationContainer.withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
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
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "true")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
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
                assertNotEquals(0, spanDemandEntry.startDiskWriteDemand);
                assertNotEquals(0, spanDemandEntry.endDiskWriteDemand);
                assertNotEquals(0, spanDemandEntry.logSystemTime);
                assertNotEquals(0, spanDemandEntry.cpuEmissionsInMg);
                assertNotEquals(0, spanDemandEntry.memoryEmissionsInMg);
                assertNotEquals(0, spanDemandEntry.embodiedEmissionsInMg);
                assertTrue(spanDemandEntry.storageEmissionsInMg >= 0.0); //allow both because start up might create disk operations, while no disk operations are done in the sampleapplication
            }
        }
        for (MetricDemand md : metricDemands) {
            assertNotEquals(0.0, md.cpuEmissionsInMg);
            assertNotEquals(0.0, md.memoryEmissionsInMg);
            assertNotEquals(0.0, md.storageEmissionsInMg);
            assertNotEquals(0.0, md.embodiedEmissionsInMg);
        }
    }


    @Test
    void testOnlyCPUDemand() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "false")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (List<SpanDemand> sds : spanDemands.values()) {
            assertEquals(1, sds.size());
            SpanDemand sd = sds.get(0);
            assertNotEquals(0, sd.startCpuTime);
            assertNotEquals(0, sd.endCpuTime);
            assertNotEquals(0, sd.cpuEmissionsInMg);
            assertNotEquals(0, sd.embodiedEmissionsInMg);
            assertNull(sd.startHeapDemand);
            assertNull(sd.endHeapDemand);
            assertNull(sd.totalHeapSize);
            assertNull(sd.startDiskReadDemand);
            assertNull(sd.endDiskReadDemand);
            assertNull(sd.startDiskWriteDemand);
            assertNull(sd.endDiskWriteDemand);
            assertEquals(0.0, sd.memoryEmissionsInMg);
            assertEquals(0.0, sd.storageEmissionsInMg);
        }
    }

    @Test
    void testOnlyCpuMetricDemands() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "false")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
        executeContainer();
        assertFalse(metricDemands.isEmpty());
        for (MetricDemand md : metricDemands) {
            assertNotEquals(0.0, md.cpuEmissionsInMg);
            assertNotEquals(0.0, md.embodiedEmissionsInMg);
            assertEquals(0.0, md.memoryEmissionsInMg);
            assertEquals(0.0, md.storageEmissionsInMg);
        }
    }


    @Test
    void testOnlyMemoryMetricDemands() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "false")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
        executeContainer();
        assertFalse(metricDemands.isEmpty());
        for (MetricDemand md : metricDemands) {
            assertNotEquals(0.0, md.memoryEmissionsInMg);
            assertEquals(0.0, md.cpuEmissionsInMg);
            assertEquals(0.0, md.storageEmissionsInMg);
            assertEquals(0.0, md.embodiedEmissionsInMg);
        }
    }

    @Test
    void testOnlyStorageMetricDemands() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "false")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
        executeContainer();
        assertFalse(metricDemands.isEmpty());
        for (MetricDemand md : metricDemands) {
            assertNotEquals(0.0, md.storageEmissionsInMg);
            assertEquals(0.0, md.cpuEmissionsInMg);
            assertEquals(0.0, md.memoryEmissionsInMg);
            assertEquals(0.0, md.embodiedEmissionsInMg);
        }
    }

    @Test
    void testOnlyLogSystem() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "false")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
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
            assertEquals(0.0, sd.cpuEmissionsInMg);
            assertEquals(0.0, sd.memoryEmissionsInMg);
            assertEquals(0.0, sd.storageEmissionsInMg);
            assertEquals(0.0, sd.embodiedEmissionsInMg);
        }
    }

    @Test
    void testOnlyHeapDemand() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "false")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
        executeContainer();

        Assertions.assertTrue(!spanDemands.entrySet().isEmpty());
        for (List<SpanDemand> sds : spanDemands.values()) {
            assertEquals(1, sds.size());
            SpanDemand sd = sds.get(0);
            assertNull(sd.startCpuTime);
            assertNull(sd.endCpuTime);
            assertNotEquals(0, sd.startHeapDemand);
            assertNotEquals(0, sd.endHeapDemand);
            assertNotEquals(0, sd.memoryEmissionsInMg);
            assertNull(sd.totalHeapSize);
            assertNull(sd.startDiskReadDemand);
            assertNull(sd.endDiskReadDemand);
            assertNull(sd.startDiskWriteDemand);
            assertNull(sd.endDiskWriteDemand);
            assertEquals(0.0, sd.cpuEmissionsInMg);
            assertEquals(0.0, sd.storageEmissionsInMg);
            assertEquals(0.0, sd.embodiedEmissionsInMg);
        }
    }

    @Test
    void testOnlyGCDemands() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "true")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
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
                    assertEquals(0.0, spanDemandEntry.memoryEmissionsInMg);
                    assertEquals(0.0, spanDemandEntry.cpuEmissionsInMg);
                    assertEquals(0.0, spanDemandEntry.embodiedEmissionsInMg);
                    assertEquals(0.0, spanDemandEntry.storageEmissionsInMg);
                } else {
                    assertNotEquals(0, spanDemandEntry.startHeapDemand);
                    assertNotEquals(0, spanDemandEntry.endHeapDemand);
                    assertNotEquals(0, spanDemandEntry.totalHeapSize);
                }
                assertNull(spanDemandEntry.startDiskReadDemand);
                assertNull(spanDemandEntry.endDiskReadDemand);
                assertNull(spanDemandEntry.startDiskWriteDemand);
                assertNull(spanDemandEntry.endDiskWriteDemand);
            }
        }
    }

    @Test
    void testOnlyDiskDemand() {
        applicationContainer.withEnv("DE_RETIT_APM_LOG_CPU_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_DISK_DEMAND", "true")
                .withEnv("DE_RETIT_APM_LOG_HEAP_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_NETWORK_DEMAND", "false")
                .withEnv("DE_RETIT_APM_LOG_GC_EVENT", "false")
                .withEnv("SERVICE_NAME", "testService")
                .withEnv("STORAGE_TYPE", "SSD")
                .withEnv("REGION", "af-south-1")
                .withEnv("INSTANCE", "a1.medium")
                .withEnv("CLOUD_PROVIDER", "AWS");
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
            assertEquals(0.0, sd.memoryEmissionsInMg);
            assertEquals(0.0, sd.cpuEmissionsInMg);
            assertEquals(0.0, sd.embodiedEmissionsInMg);
            assertNotEquals(0, sd.startDiskReadDemand);
            assertNotEquals(0, sd.endDiskReadDemand);
            assertNotEquals(0, sd.startDiskWriteDemand);
            assertNotEquals(0, sd.endDiskWriteDemand);
            assertTrue(sd.storageEmissionsInMg >= 0); //allow both because start up might create disk operations, while no disk operations are done in the sampleapplication
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
        if (logOutput.contains("io.opentelemetry.exporter.logging.LoggingMetricExporter") && logOutput.contains("Servicecall")){
            LOGGER.info("hier ist er" + logOutput);
            String[] keys = {"cpuEmissionsInMg", "memoryEmissionsInMg", "storageEmissionsInMg", "embodiedEmissionsInMg"};
            MetricDemand demand = extractMetricValuesFromLog(logOutput, keys);
            metricDemands.add(demand);
        }
    }

    /*private static void addToMetricDemands(String logOutput) {
        if (logOutput.contains("io.opentelemetry.exporter.logging.LoggingMetricExporter")) {
            LOGGER.info("hier ist er" + logOutput);
            String[] keys = {"cpuEmissionsInMg", "memoryEmissionsInMg", "storageEmissionsInMg", "embodiedEmissionsInMg"};
            Map<String, Double> extractedValues = extractMetricValuesFromLog(logOutput, keys);
            metricDemands.putAll(extractedValues);
        }
    }

     */

    private static MetricDemand extractMetricValuesFromLog(String logMessage, String[] keys) {
        MetricDemand demand = new MetricDemand();
        for (String key : keys) {
            LOGGER.info("Hier ist der key " + key);
            String keyWithEqual = key + "=";
            double value = 0.0;

            int startIndex = logMessage.indexOf(keyWithEqual);
            if (startIndex != -1) {
                startIndex += keyWithEqual.length();
                int endIndex = logMessage.indexOf(',', startIndex);
                if (endIndex == -1) {
                    endIndex = logMessage.indexOf('}', startIndex);
                }
                if (endIndex != -1) {
                    String valueStr = logMessage.substring(startIndex, endIndex).trim();
                    try {
                        valueStr = valueStr.replaceAll("[^0-9.]", "");
                        value = Double.parseDouble(valueStr);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing " + key + ": " + e.getMessage());
                    }
                }
            }
            LOGGER.info("Hier ist der value " + value);
            switch (key) {
                case "cpuEmissionsInMg":
                    demand.cpuEmissionsInMg = value;
                    break;
                case "memoryEmissionsInMg":
                    demand.memoryEmissionsInMg = value;
                    break;
                case "storageEmissionsInMg":
                    demand.storageEmissionsInMg = value;
                    break;
                case "embodiedEmissionsInMg":
                    demand.embodiedEmissionsInMg = value;
                    break;
            }
        }
        return demand;
    }
    /*
    private static Map<String, Double> extractMetricValuesFromLog(String logMessage, String[] keys) {
        Map<String, Double> values = new HashMap<>();
        for (String key : keys) {
            LOGGER.info("Hier ist der key " + key);
            String keyWithEqual = key + "=";
            double value = 0.0;

            int startIndex = logMessage.indexOf(keyWithEqual);
            if (startIndex != -1) {
                startIndex += keyWithEqual.length();
                int endIndex = logMessage.indexOf(',', startIndex);
                if (endIndex == -1) {
                    endIndex = logMessage.indexOf('}', startIndex);
                }
                if (endIndex != -1) {
                    String valueStr = logMessage.substring(startIndex, endIndex).trim();
                    valueStr = valueStr.replaceAll("[^0-9.]", "");
                    value = Double.parseDouble(valueStr);
                }
            }
            values.put(key, value);
            LOGGER.info("Hier ist der value " + value);
        }
        return values;
    }

     */


    private static String extractOperationNameFromLogOutput(String logOutput) {
        return logOutput.substring(logOutput.indexOf('\'') + 1, logOutput.lastIndexOf('\''));
    }

    private static SpanDemand parseProperties(String logOutput) {
        SpanDemand spanDemand = new SpanDemand();
        String[] properties = extractPropertiesFromLogOutput(logOutput);
        for (String prop : properties) {
            String[] elems = prop.split("=");
            if (elems[0].contains("de.retit.endcputime")) {
                spanDemand.endCpuTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.enddiskreaddemand")) {
                spanDemand.endDiskReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.enddiskwritedemand")) {
                spanDemand.endDiskWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.endheapbyteallocation")) {
                spanDemand.endHeapDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.endsystemtime")) {
                spanDemand.endSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.startcputime")) {
                spanDemand.startCpuTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.startdiskreaddemand")) {
                spanDemand.startDiskReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.startdiskwritedemand")) {
                spanDemand.startDiskWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.startheapbyteallocation")) {
                spanDemand.startHeapDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.startsystemtime")) {
                spanDemand.startSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.logsystemtime")) {
                spanDemand.logSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("de.retit.totalheapsize")) {
                spanDemand.totalHeapSize = Long.valueOf(elems[1]);
            } else if (elems[0].contains("cpuEmissionsInMg")) {
                spanDemand.cpuEmissionsInMg = Double.valueOf(elems[1]);
            } else if (elems[0].contains("memoryEmissionsInMg")) {
                spanDemand.memoryEmissionsInMg = Double.valueOf(elems[1]);
            } else if (elems[0].contains("storageEmissionsInMg")) {
                spanDemand.storageEmissionsInMg = Double.valueOf(elems[1]);
            } else if (elems[0].contains("embodiedEmissionsInMg")) {
                spanDemand.embodiedEmissionsInMg = Double.valueOf(elems[1]);
            }
        }
        return spanDemand;
    }

    private static String[] extractPropertiesFromLogOutput(String logOutput) {
        return logOutput.substring(logOutput.indexOf('{') + 1, logOutput.lastIndexOf('}')).split(",");
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
        public Long logSystemTime = null;
        public Long totalHeapSize = null;
        public Double cpuEmissionsInMg = null;
        public Double memoryEmissionsInMg = null;
        public Double storageEmissionsInMg = null;
        public Double embodiedEmissionsInMg = null;
    }

    private static class MetricDemand {
        public Double cpuEmissionsInMg = null;
        public Double memoryEmissionsInMg = null;
        public Double storageEmissionsInMg = null;
        public Double embodiedEmissionsInMg = null;
    }
}
