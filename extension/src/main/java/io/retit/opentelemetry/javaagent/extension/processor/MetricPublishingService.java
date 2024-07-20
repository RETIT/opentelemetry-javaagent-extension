package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.cpu.CpuEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.cpu.EmbodiedEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.memory.MemoryEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.storage.StorageEmissions;

public class MetricPublishingService {

    private static final LongHistogram storageEmissionMeter;
    private static final LongHistogram cpuEmissionMeter;
    private static final LongHistogram embeddedEmissionMeter;
    private static final LongHistogram memoryEmissionMeter;

    static {
        Meter meter = GlobalOpenTelemetry.get().getMeter("instrumentation-library-name");

        storageEmissionMeter = meter.histogramBuilder("storage_emissions")
                .setDescription("Total emissions from storage")
                .setUnit("gCO2e")
                .ofLongs()
                .build();

        cpuEmissionMeter = meter.histogramBuilder("cpu_emissions")
                .setDescription("Total emissions from CPU")
                .setUnit("gCO2e")
                .ofLongs()
                .build();

        embeddedEmissionMeter = meter.histogramBuilder("embedded_emissions")
                .setDescription("Total emissions from embedded components")
                .setUnit("gCO2e")
                .ofLongs()
                .build();

        memoryEmissionMeter = meter.histogramBuilder("memory_emissions")
                .setDescription("Total emissions from memory")
                .setUnit("gCO2e")
                .ofLongs()
                .build();
    }

    public static void publishStorageEmissions(double totalStorageDemand) {
        double totalEmissions = StorageEmissions.getInstance().calculateStorageEmissionsInGramm(totalStorageDemand);
        // Retrieve the cloud provider from the ConfigLoader
        //String cloudProvider = configLoader.getCloudProvider().toString();
        // Record the emissions along with the cloud provider as an attribute
        storageEmissionMeter.record((long) totalEmissions);
        System.out.println("Total storage emissions: " + totalEmissions + " on ");
    }

    public static void publishCpuEmissions(double totalCpuDemand) {
        double totalEmissions = CpuEmissions.getInstance().calculateCpuEmissionsInGramm(totalCpuDemand);
        cpuEmissionMeter.record((long) totalEmissions, Attributes.empty());
        System.out.println("Total CPU emissions: " + totalEmissions);
    }

    public static void publishEmbeddedEmissions(long totalCPUTimeUsedInHours) {
        double totalEmbodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInGramm(totalCPUTimeUsedInHours);
        embeddedEmissionMeter.record((long) totalEmbodiedEmissions, Attributes.empty());
        System.out.println("Total embodied emissions: " + totalEmbodiedEmissions);
    }

    public static void publishMemoryEmissions(double totalMemoryDemand) {
        double totalMemoryEmissions = MemoryEmissions.getInstance().calculateMemoryEmissionsInGramm(totalMemoryDemand);
        memoryEmissionMeter.record((long) totalMemoryEmissions, Attributes.empty());
        System.out.println("Total memory emissions: " + totalMemoryEmissions);
    }

    public static void publishEmissions(ReadableSpan readableSpan, double totalStorageDemand, long totalCpuTimeUsedInHours, double totalHeapDemand) {
        System.out.println("total storage demand: " + totalStorageDemand);
        System.out.println("total cpu time used: " + totalCpuTimeUsedInHours);
        System.out.println("total heap demand: " + totalHeapDemand);
            publishStorageEmissions(totalStorageDemand);
            publishCpuEmissions(totalCpuTimeUsedInHours);
            publishEmbeddedEmissions(totalCpuTimeUsedInHours);
            publishMemoryEmissions(totalHeapDemand);
    }

    private static boolean isTopLevelSpan(ReadableSpan span) {
        SpanContext parentContext = span.getParentSpanContext();
        return parentContext == null || !parentContext.isValid();
    }
}
