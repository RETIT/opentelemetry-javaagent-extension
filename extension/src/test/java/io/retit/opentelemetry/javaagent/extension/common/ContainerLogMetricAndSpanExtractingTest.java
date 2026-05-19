package io.retit.opentelemetry.javaagent.extension.common;

import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class ContainerLogMetricAndSpanExtractingTest {
    // Metrics to be tested
    protected static final String[] METRIC_NAMES = {Constants.SPAN_ATTRIBUTE_PROCESS_CPU_TIME, "io.retit.emissions.cpu.power.min", "io.retit.emissions.cpu.power.max",
            "io.retit.emissions.embodied.emissions.minute.mg", "io.retit.emissions.memory.energy.gb.minute",
            "io.retit.emissions.storage.energy.gb.minute", "io.retit.emissions.network.energy.gb.minute",
            "io.retit.emissions.pue", "io.retit.emissions.gef", "io.retit.resource.demand.storage.bytes",
            "io.retit.resource.demand.memory.bytes", "io.retit.resource.demand.network.bytes",
            "io.retit.resource.demand.cpu.ms"
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerLogMetricAndSpanExtractingTest.class);
    protected GenericContainer<?> applicationContainer;
    protected Map<String, List<SpanDemand>> spanDemands = new HashMap<>();
    protected List<MetricDemand> metricDemands = new ArrayList<>();
    protected String CONTAINER_URL = "http://localhost:";

    @NotNull
    protected static boolean isGcSpanName(String s) {
        return Constants.JAVA_AGENT_GC_OPERATION_NAME_MAJOR_FREE.equals(s) || Constants.JAVA_AGENT_GC_OPERATION_NAME_MINOR_FREE.equals(s);
    }

    protected void executeContainer(final int portToOpen) {
        // Start container and attach parser to log output
        applicationContainer.start();
        if (portToOpen != -1) {
            CONTAINER_URL = CONTAINER_URL + applicationContainer.getMappedPort(portToOpen);
        }
        applicationContainer.followOutput(outputFrame -> {
            String logOutput = outputFrame.getUtf8String();
            addToSpanDemands(logOutput);
            addToMetricDemands(logOutput);
        });
    }

    private void addToSpanDemands(String logOutput) {
        // in case of span
        if (logOutput.contains("io.opentelemetry.exporter.logging.LoggingSpanExporter")) {
            String operationName = extractOperationNameFromLogOutput(logOutput);
            SpanDemand spanDemand = SpanDemand.parseProperties(logOutput);
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

    private void addToMetricDemands(String logOutput) {
        if (logOutput.contains("io.opentelemetry.exporter.logging.LoggingMetricExporter")) {
            LOGGER.info("Processing metric " + logOutput);
            List<MetricDemand> demands = MetricDemand.extractMetricValuesFromLog(logOutput);
            if (demands != null) {
                metricDemands.addAll(demands);
            }
        }
    }

    private String extractOperationNameFromLogOutput(String logOutput) {
        LOGGER.info("Processing span " + logOutput);
        return logOutput.substring(logOutput.indexOf('\'') + 1, logOutput.lastIndexOf('\''));
    }

    protected void waitUntilContainerIsStopped() {
        // Wait until container has stopped running
        while (applicationContainer.isRunning()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("Interupted", e);
            }
        }
    }

    protected void assertFullSpanDataContent(final String sampleMethod) {
        Assertions.assertFalse(spanDemands.isEmpty());
        for (Map.Entry<String, List<SpanDemand>> spanDemandEntryList : spanDemands.entrySet()) {
            if ("<unspecified span name>".equals(spanDemandEntryList.getKey())) {
                continue;
            } else if (!isGcSpanName(spanDemandEntryList.getKey())) {
                Assertions.assertEquals(1, spanDemandEntryList.getValue().size());
            }
            for (SpanDemand spanDemandEntry : spanDemandEntryList.getValue()) {
                Assertions.assertNotEquals(0, spanDemandEntry.startCpuTime);
                Assertions.assertNotEquals(0, spanDemandEntry.endCpuTime);
                Assertions.assertNotEquals(0, spanDemandEntry.startHeapDemand);
                Assertions.assertNotEquals(0, spanDemandEntry.endHeapDemand);
                if (spanDemandEntryList.getKey().contains(sampleMethod)) {
                    Assertions.assertNull(spanDemandEntry.totalHeapSize);
                } else {
                    Assertions.assertNotEquals(0, spanDemandEntry.totalHeapSize);
                }
                Assertions.assertNotEquals(0, spanDemandEntry.startDiskReadDemand);
                Assertions.assertNotEquals(0, spanDemandEntry.endDiskReadDemand);
                if (!isGcSpanName(spanDemandEntryList.getKey())) {
                    if (spanDemandEntry.endDiskWriteDemand != 0) {
                        Assertions.assertNotEquals(0, spanDemandEntry.endDiskWriteDemand);
                        Assertions.assertNotEquals(0, spanDemandEntry.endDiskWriteDemand - spanDemandEntry.startDiskWriteDemand);
                    }
                }
                Assertions.assertNotEquals(0, spanDemandEntry.logSystemTime);

                Assertions.assertNotNull(spanDemandEntry.startNetworkReadDemand);
                Assertions.assertNotNull(spanDemandEntry.endNetworkReadDemand);
                Assertions.assertNotNull(spanDemandEntry.startNetworkWriteDemand);
                Assertions.assertNotNull(spanDemandEntry.endNetworkWriteDemand);
                Assertions.assertNotEquals(0, spanDemandEntry.startThreadId);
                Assertions.assertNotEquals(0, spanDemandEntry.endThreadId);
            }
        }
    }
}
