/*
 *   Copyright 2024 RETIT GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.retit.opentelemetry.javaagent.extension.jdk;

import io.retit.opentelemetry.javaagent.extension.common.ContainerLogMetricAndSpanExtractingTest;
import io.retit.opentelemetry.javaagent.extension.common.MetricDemand;
import io.retit.opentelemetry.javaagent.extension.common.SpanDemand;
import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
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


abstract class JavaAgentExtensionIT extends ContainerLogMetricAndSpanExtractingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaAgentExtensionIT.class);
    // Operations in SampleApplication
    private static final String SAMPLE_METHOD = "SampleApplication.method";
    private static final String SAMPLE_METHOD1 = "SampleApplication.method1";
    private static final String SAMPLE_METHOD2 = "SampleApplication.method2";


    protected void commonSetup(final String imageName) {
        LOGGER.info("Using image: " + imageName);
        applicationContainer = new GenericContainer<>(imageName)
                .withEnv("OTEL_LOGS_EXPORTER", "none")
                .withEnv("OTEL_METRICS_EXPORTER", "console")
                .withEnv("OTEL_TRACES_EXPORTER", "console")
                .withEnv("JAVA_TOOL_OPTIONS", "-javaagent:opentelemetry-javaagent-all.jar -Dotel.javaagent.extensions=io.retit.opentelemetry.javaagent.extension.jar");
        spanDemands = new HashMap<>();
        metricDemands = new ArrayList<>();
    }

    protected void executeContainer() {
        super.executeContainer(-1);

        super.waitUntilContainerIsStopped();
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
                        e -> !SAMPLE_METHOD1.equals(e.getKey()) && !SAMPLE_METHOD2.equals(e.getKey()) && !"<unspecified span name>".equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertTrue(gcSpans.keySet().stream()
                .allMatch(s -> isGcSpanName(s)));
        LOGGER.info("Found {} gc spans", gcSpans.values().stream().mapToInt(List::size).sum());
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

        assertFullSpanDataContent(SAMPLE_METHOD);

        Assertions.assertTrue(!metricDemands.isEmpty());

        for (String metricName : METRIC_NAMES) {
            LOGGER.info("Asserting metric {}", metricName);
            Optional<MetricDemand> metricDemandResult = metricDemands.stream().filter(metricDemand -> metricName.equals(metricDemand.metricName)).findFirst();
            LOGGER.info("Found metric {} with value {}", metricName, metricDemandResult.get().metricValue);
            Assertions.assertTrue(metricDemandResult.isPresent());
            Assertions.assertNotNull(metricDemandResult.get().metricValue);
            if (!"io.retit.resource.demand.network.bytes".equals(metricDemandResult.get().metricName)) {

                assertNotEquals(0.0, metricDemandResult.get().metricValue);
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
            assertNotEquals(-1, sd.startCpuTime);
            assertNotEquals(-1, sd.endCpuTime);
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
            assertNotEquals(0, sd.startThreadId);
            assertNotEquals(0, sd.endThreadId);
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
            assertNotEquals(-1, sd.startHeapDemand);
            assertNotEquals(-1, sd.endHeapDemand);
            assertNull(sd.totalHeapSize);
            assertNull(sd.startDiskReadDemand);
            assertNull(sd.endDiskReadDemand);
            assertNull(sd.startDiskWriteDemand);
            assertNull(sd.endDiskWriteDemand);
            assertNull(sd.startNetworkReadDemand);
            assertNull(sd.endNetworkReadDemand);
            assertNull(sd.startNetworkWriteDemand);
            assertNull(sd.endNetworkWriteDemand);
            assertNotEquals(0, sd.startThreadId);
            assertNotEquals(0, sd.endThreadId);
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
                    assertNotEquals(0, spanDemandEntry.startThreadId);
                    assertNotEquals(0, spanDemandEntry.endThreadId);
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
            assertNotEquals(0, sd.startThreadId);
            assertNotEquals(0, sd.endThreadId);
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
            assertNotEquals(0, sd.startThreadId);
            assertNotEquals(0, sd.endThreadId);
        }
    }

}
