package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.common.Attributes;
import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.cpu.CpuEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.cpu.EmbodiedEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.memory.MemoryEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.storage.StorageEmissions;

public class MetricPublishingService {

    public static MetricPublishingService instance;

    private final LongCounter storageEmissionMeter;
    private final LongCounter cpuEmissionMeter;
    private final LongCounter embeddedEmissionMeter;
    private final LongCounter memoryEmissionMeter;

    private final ConfigLoader configLoader;

    public static MetricPublishingService getInstance() {
        if (instance == null) {
            instance = new MetricPublishingService();
        }
        return instance;
    }

    private MetricPublishingService() {
        configLoader = ConfigLoader.getConfigInstance();

        Meter meter = GlobalOpenTelemetry.get().getMeter("instrumentation-library-name");

        storageEmissionMeter = meter.counterBuilder("storage_emissions")
                .setDescription("Total emissions from storage")
                .setUnit("gCO2e")
                .build();

        cpuEmissionMeter = meter.counterBuilder("cpu_emissions")
                .setDescription("Total emissions from CPU")
                .setUnit("gCO2e")
                .build();

        embeddedEmissionMeter = meter.counterBuilder("embedded_emissions")
                .setDescription("Total emissions from embedded components")
                .setUnit("gCO2e")
                .build();

        memoryEmissionMeter = meter.counterBuilder("memory_emissions")
                .setDescription("Total emissions from memory")
                .setUnit("gCO2e")
                .build();
    }

    public void publishStorageEmissions(double totalStorageDemand, Attributes attributes) {
        double totalEmissions = StorageEmissions.getInstance().calculateStorageEmissionsInGramm(totalStorageDemand);
        AttributesBuilder attributesBuilder = attributes.toBuilder();
        attributesBuilder.put(AttributeKey.stringKey("region"), configLoader.getRegion());
        attributesBuilder.put(AttributeKey.stringKey("instance-type"), configLoader.getCloudInstanceName());
        attributesBuilder.put(AttributeKey.stringKey("provider"), configLoader.getCloudProvider().toString());
        attributesBuilder.put(AttributeKey.stringKey("storage-type"), configLoader.getStorageType().toString());
        storageEmissionMeter.add((long) totalEmissions, attributes);
        System.out.println("Total storage emissions: " + totalEmissions + " on ");
    }

    public void publishCpuEmissions(double totalCpuDemand, Attributes attributes) {
        double totalEmissions = CpuEmissions.getInstance().calculateCpuEmissionsInGramm(totalCpuDemand);
        AttributesBuilder attributesBuilder = attributes.toBuilder();
        attributesBuilder.put(AttributeKey.stringKey("region"), configLoader.getRegion());
        attributesBuilder.put(AttributeKey.stringKey("instance-type"), configLoader.getCloudInstanceName());
        attributesBuilder.put(AttributeKey.stringKey("provider"), configLoader.getCloudProvider().toString());
        cpuEmissionMeter.add((long) totalEmissions, attributes);
        System.out.println("Total CPU emissions: " + totalEmissions);
    }

    public void publishEmbeddedEmissions(double totalCPUTimeUsedInHours, Attributes attributes) {
        double totalEmbodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInGramm(totalCPUTimeUsedInHours);
        AttributesBuilder attributesBuilder = attributes.toBuilder();
        attributesBuilder.put(AttributeKey.stringKey("region"), configLoader.getRegion());
        attributesBuilder.put(AttributeKey.stringKey("instance-type"), configLoader.getCloudInstanceName());
        attributesBuilder.put(AttributeKey.stringKey("provider"), configLoader.getCloudProvider().toString());
        embeddedEmissionMeter.add((long) totalEmbodiedEmissions, attributes);
        System.out.println("Total embodied emissions: " + totalEmbodiedEmissions);
    }

    public void publishMemoryEmissions(double totalMemoryDemand, Attributes attributes) {
        double totalMemoryEmissions = MemoryEmissions.getInstance().calculateMemoryEmissionsInGramm(totalMemoryDemand);
        AttributesBuilder attributesBuilder = attributes.toBuilder();
        attributesBuilder.put(AttributeKey.stringKey("region"), configLoader.getRegion());
        attributesBuilder.put(AttributeKey.stringKey("instance-type"), configLoader.getCloudInstanceName());
        attributesBuilder.put(AttributeKey.stringKey("provider"), configLoader.getCloudProvider().toString());
        memoryEmissionMeter.add((long) totalMemoryEmissions, attributes);
        System.out.println("Total memory emissions: " + totalMemoryEmissions);
    }

    public void publishEmissions(Attributes attributes, double totalStorageDemand, long totalCpuTimeUsedInHours, double totalHeapDemand) {
        publishStorageEmissions(totalStorageDemand, attributes);
        publishCpuEmissions(totalCpuTimeUsedInHours, attributes);
        publishEmbeddedEmissions(totalCpuTimeUsedInHours, attributes);
        publishMemoryEmissions(totalHeapDemand, attributes);
    }
}
