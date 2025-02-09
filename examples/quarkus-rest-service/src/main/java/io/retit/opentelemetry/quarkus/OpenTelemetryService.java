package io.retit.opentelemetry.quarkus;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * This service is used to publish resource demand and CO2 emission data as OpenTelemetry metrics.
 */
@ApplicationScoped
public class OpenTelemetryService {

    /**
     * This method publishes the CPU demand as OpenTelemetry metric.
     *
     * @param cpuTimeInMS - the CPU time in milliseconds.
     * @param attributes  - the attributes to be published along with the CPU time metric.
     */
    public void publishCpuTimeMetric(final long cpuTimeInMS, final Attributes attributes) {
        // Record data

        getLongCounter("cpu_demand",
                "CPU Demand Metric", "ms")
                .add(cpuTimeInMS, attributes);
    }

    /**
     * This method publishes the memory demand as OpenTelemetry metric.
     *
     * @param memoryDemandInKByte - the memory demand in kilobytes.
     * @param attributes          - the attributes to be published along with the memory demand metric.
     */
    public void publishMemoryDemandMetric(final long memoryDemandInKByte, final Attributes attributes) {
        // Record data
        getLongCounter("memory_demand",
                "Memory Demand Metric", "kByte")
                .add(memoryDemandInKByte * 1024, attributes);
    }

    /**
     * This method publishes the call count of  specific service as OpenTelemetry metric. Each
     * individual call is tracked as a single call, therefore there is no call count parameter.
     *
     * @param attributes - the attributes to be published along with the memory demand metric.
     */
    public void publishCallCountMetric(final Attributes attributes) {
        // Record data
        getLongCounter("call_count",
                "Tracks the number of calls to a service", "1")
                .add(1, attributes);
    }

    /**
     * Returns the OpenTelemetry Meter that is required to publish OpenTelemetry metrics.
     *
     * @return - an OpenTelemetry Meter instance.
     */
    private Meter getOpenTelemetryMeter() {
        // Gets or creates a named meter instance
        return GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();
    }

    /**
     * Creates an OpenTelemetry LongCounter with the provided name, description and unit.
     *
     * @param counterName - the name of the LongCounter.
     * @param description - the description of the LongCounter.
     * @param unit        - the unit of the LongCounter.
     * @return a LongCounter instance.
     */
    private LongCounter getLongCounter(final String counterName, final String description, final String unit) {
        return getOpenTelemetryMeter()
                .counterBuilder(counterName)
                .setDescription(description)
                .setUnit(unit)
                .build();
    }

}
