package io.retit.opentelemetry.quarkus.library;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Publishes resource demand and CO2 emission data as OpenTelemetry metrics.
 */
@ApplicationScoped
public class OpenTelemetryService {

    /**
     * Publishes CPU time as an OpenTelemetry metric.
     *
     * @param cpuTimeInMS CPU time in milliseconds.
     * @param attributes  metric attributes.
     */
    public void publishCpuTimeMetric(final long cpuTimeInMS, final Attributes attributes) {
        getLongCounter("cpu_demand", "CPU Demand Metric", "ms").add(cpuTimeInMS, attributes);
    }

    /**
     * Publishes memory demand as an OpenTelemetry metric.
     *
     * @param memoryDemandInKByte memory demand in kilobytes.
     * @param attributes          metric attributes.
     */
    public void publishMemoryDemandMetric(final long memoryDemandInKByte, final Attributes attributes) {
        getLongCounter("memory_demand", "Memory Demand Metric", "kByte")
                .add(memoryDemandInKByte * 1024, attributes);
    }

    /**
     * Increments the call count metric by one.
     *
     * @param attributes metric attributes.
     */
    public void publishCallCountMetric(final Attributes attributes) {
        getLongCounter("call_count", "Tracks the number of calls to a service", "1").add(1, attributes);
    }

    private Meter getOpenTelemetryMeter() {
        return GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();
    }

    private LongCounter getLongCounter(final String counterName, final String description, final String unit) {
        return getOpenTelemetryMeter()
                .counterBuilder(counterName)
                .setDescription(description)
                .setUnit(unit)
                .build();
    }
}