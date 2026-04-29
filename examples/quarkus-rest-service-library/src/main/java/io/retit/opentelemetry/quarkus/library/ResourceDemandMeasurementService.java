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

package io.retit.opentelemetry.quarkus.library;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Measures CPU and memory resource demand of the current thread and publishes the results
 * as OpenTelemetry metrics via {@link OpenTelemetryServiceOld}.
 */
@ApplicationScoped
public class ResourceDemandMeasurementService {

    @Inject
    private OpenTelemetryService otelService;

    /**
     * Snapshot of thread-level metrics.
     */
    public static final class Measurement {

        private final long cpuTime;

        private final long bytes;

        /**
         * Creates a new resource-demand snapshot.
         *
         * @param cpuTime thread CPU time in nanoseconds.
         * @param bytes   heap bytes allocated by thread.
         */
        public Measurement(final long cpuTime, final long bytes) {
            this.cpuTime = cpuTime;
            this.bytes = bytes;
        }

        /**
         * Returns the CPU time captured in this snapshot.
         *
         * @return CPU time in nanoseconds.
         */
        public long getCpuTime() {
            return cpuTime;
        }

        /**
         * Returns the heap allocation captured in this snapshot.
         *
         * @return bytes allocated.
         */
        public long getBytes() {
            return bytes;
        }
    }

    /**
     * Returns the CPU time of the current thread.
     *
     * @return CPU time in nanoseconds.
     */
    protected long getCurrentThreadCpuTime() {
        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    }

    /**
     * Returns the cumulative heap bytes allocated by the current thread.
     *
     * @return allocated bytes.
     */
    protected long getCurrentThreadMemoryConsumption() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        com.sun.management.ThreadMXBean sunThreadMXBean = (com.sun.management.ThreadMXBean) mxBean;
        return sunThreadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    /**
     * Takes a snapshot of the current thread's CPU time and memory allocation.
     *
     * @return a {@link Measurement} snapshot.
     */
    public Measurement measure() {
        return new Measurement(getCurrentThreadCpuTime(), getCurrentThreadMemoryConsumption());
    }

    /**
     * Calculates resource demand relative to a start snapshot and publishes metrics.
     *
     * @param startMeasurement - measurement taken at the start of the operation.
     * @param httpMethod       - HTTP method tag added to the metric attributes.
     * @return the {@link Attributes} recorded with the metrics.
     */
    public Attributes measureAndPublishMetrics(final Measurement startMeasurement, final String httpMethod) {
        Measurement endMeasurement = measure();
        Attributes attributes = Attributes.of(AttributeKey.stringKey("httpmethod"), httpMethod);
        otelService.publishCpuTimeMetric(
                (endMeasurement.getCpuTime() - startMeasurement.getCpuTime()) / 1_000_000, attributes);
        otelService.publishMemoryDemandMetric(
                (endMeasurement.getBytes() - startMeasurement.getBytes()) / 1_000, attributes);
        otelService.publishCallCountMetric(attributes);
        return attributes;
    }
}