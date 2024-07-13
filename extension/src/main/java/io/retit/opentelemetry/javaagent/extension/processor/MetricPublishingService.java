package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu.CpuEmissions;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu.EmbodiedEmissions;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.memory.MemoryEmissions;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage.StorageEmissions;

public class MetricPublishingService {

    private final LongHistogram storageEmissionMeter;
    private final LongHistogram cpuEmissionMeter;
    private final LongHistogram embeddedEmissionMeter;
    private final LongHistogram memoryEmissionMeter;

    private MetricPublishingService() {
        Meter meter = GlobalOpenTelemetry.get().getMeter("instrumentation-library-name");

        storageEmissionMeter = meter.histogramBuilder("storage_emissions")
                .setDescription("total emissions from storage")
                .setUnit("kgCO2e")
                .ofLongs().build();

        cpuEmissionMeter = meter.histogramBuilder("cpu_emissions")
                .setDescription("total emissions from cpu")
                .setUnit("kgCO2e")
                .ofLongs().build();

        embeddedEmissionMeter = meter.histogramBuilder("embedded_emissions")
                .setDescription("total emissions from embedded")
                .setUnit("kgCO2e")
                .ofLongs().build();

        memoryEmissionMeter = meter.histogramBuilder("memory_emissions")
                .setDescription("total emissions from memory")
                .setUnit("kgCO2e")
                .ofLongs().build();
    }

    private static void publishStorageEmissions(double totalStorageDemand) {
        double totalEmissions = StorageEmissions.getInstance().calculateStorageEmissions(totalStorageDemand);
        System.out.println("total storage emissions: " + totalEmissions);
    }

    private static void publishCpuEmissions(double totalCpuDemand) {
        double totalEmissions = CpuEmissions.getInstance().calculateCpuEmissions(totalCpuDemand);
        System.out.println("total cpu emissions: " + totalEmissions);
    }

    private static void publishEmbeddedEmissions(long totalCPUTimeUsedInHours) {
        //ignoring serverless software
        double totalEmbodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInGramm(totalCPUTimeUsedInHours);
        System.out.println("total embodied emissions: " + totalEmbodiedEmissions);
    }

    private static void publishMemoryEmissions(double totalMemoryDemand) {
        //ignoring serverless software
        double totalMemoryEmissions = MemoryEmissions.getInstance().calculateMemoryEmissions( totalMemoryDemand);
        System.out.println("total memory emissions: " + totalMemoryEmissions);
      //  memoryEmissionMeter.record((long) totalMemoryEmissions, Attributes.of(AttributeKey.stringKey("label_for_memory_demand"), "value"));
    }

    public static void publishEmissions(double totalStorageDemand, long totalCpuTimeUsedInHours, double totalHeapDemand) {
        publishStorageEmissions(totalStorageDemand);
        publishCpuEmissions(totalCpuTimeUsedInHours);
        publishEmbeddedEmissions(totalCpuTimeUsedInHours);
        publishMemoryEmissions(totalHeapDemand);
    }
}

