package io.retit.opentelemetry.quarkus;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * This service is used to publish resource demand and CO2 emission data as OpenTelemetry metrics.
 */
@ApplicationScoped
public class OpenTelemetryService {

    private final LongCounter cpuDemandCounter;
    private final LongCounter memoryDemandCounter;
    private final LongCounter callCountCounter;

    @Inject
    OpenTelemetryService(final OpenTelemetry openTelemetry) {
        Meter meter = openTelemetry.meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        cpuDemandCounter = meter.counterBuilder("cpu_demand")
                .setDescription("CPU Demand Metric")
                .setUnit("ms")
                .build();

        memoryDemandCounter = meter.counterBuilder("memory_demand")
                .setDescription("Memory Demand Metric")
                .setUnit("kByte")
                .build();

        callCountCounter = meter.counterBuilder("call_count")
                .setDescription("Tracks the number of calls to a service")
                .setUnit("1")
                .build();
    }

    /**
     * This method publishes the CPU demand as OpenTelemetry metric.
     *
     * @param cpuTimeInMS - the CPU time in milliseconds.
     * @param attributes  - the attributes to be published along with the CPU time metric.
     */
    public void publishCpuTimeMetric(final long cpuTimeInMS, final Attributes attributes) {
        cpuDemandCounter.add(cpuTimeInMS, attributes);
    }

    /**
     * This method publishes the memory demand as OpenTelemetry metric.
     *
     * @param memoryDemandInKByte - the memory demand in kilobytes.
     * @param attributes          - the attributes to be published along with the memory demand metric.
     */
    public void publishMemoryDemandMetric(final long memoryDemandInKByte, final Attributes attributes) {
        memoryDemandCounter.add(memoryDemandInKByte, attributes);
    }

    /**
     * This method publishes the call count of a specific service as OpenTelemetry metric. Each
     * individual call is tracked as a single call, therefore there is no call count parameter.
     *
     * @param attributes - the attributes to be published along with the call count metric.
     */
    public void publishCallCountMetric(final Attributes attributes) {
        callCountCounter.add(1, attributes);
    }
}
