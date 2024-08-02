package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.common.Attributes;
import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

/**
 * Handles the publishing of various emissions metrics to an OpenTelemetry meter.
 * This service is responsible for calculating and publishing emissions from storage, CPU, embedded components, and memory.
 */
public class MetricPublishingService {

    public static MetricPublishingService instance;

    private final DoubleHistogram storageEmissionMeter;
    private final DoubleHistogram cpuEmissionMeter;
    private final DoubleHistogram embeddedEmissionMeter;
    private final DoubleHistogram memoryEmissionMeter;

    private final ConfigLoader configLoader;

    /**
     * Returns the singleton instance of MetricPublishingService, creating it if necessary.
     *
     * @return The singleton instance of MetricPublishingService.
     */
    public static MetricPublishingService getInstance() {
        if (instance == null) {
            instance = new MetricPublishingService();
        }
        return instance;
    }

    /**
     * Private constructor for initializing the MetricPublishingService.
     * Initializes the meters for storage, CPU, embedded components, and memory emissions.
     */
    private MetricPublishingService() {
        configLoader = ConfigLoader.getConfigInstance();

        Meter meter = GlobalOpenTelemetry.get().getMeter("instrumentation-library-name");

        storageEmissionMeter = meter.histogramBuilder("storage_emissions")
                .setDescription("Total emissions from storage")
                .setUnit("mgCO2e")
                .build();

        cpuEmissionMeter = meter.histogramBuilder("cpu_emissions")
                .setDescription("Total emissions from CPU")
                .setUnit("mgCO2e")
                .build();

        embeddedEmissionMeter = meter.histogramBuilder("embedded_emissions")
                .setDescription("Total emissions from embedded components")
                .setUnit("mgCO2e")
                .build();

        memoryEmissionMeter = meter.histogramBuilder("memory_emissions")
                .setDescription("Total emissions from memory")
                .setUnit("mgCO2e")
                .build();
    }

    /**
     * Publishes the calculated storage emissions.
     *
     * @param storageEmissions The total demand for storage in bytes.
     * @param attributes       Additional attributes for the emission event.
     */
    public void publishStorageEmissions(Double storageEmissions, Attributes attributes) {
        if (storageEmissions == null) {
            return;
        }
        storageEmissionMeter.record(storageEmissions, attributes);
    }

    /**
     * Publishes the calculated CPU emissions.
     *
     * @param cpuEmissions The total CPU demand in milliseconds.
     * @param attributes   Additional attributes for the emission event.
     */
    public void publishCpuEmissions(Double cpuEmissions, Attributes attributes) {
        if (cpuEmissions == null) {
            return;
        }
        cpuEmissionMeter.record(cpuEmissions, attributes);
    }

    /**
     * Publishes the calculated emissions from embedded components based on CPU time used.
     *
     * @param embodiedEmissions The total CPU time used in hours.
     * @param attributes        Additional attributes for the emission event.
     */
    public void publishEmbeddedEmissions(Double embodiedEmissions, Attributes attributes) {
        if (embodiedEmissions == null) {
            return;
        }
        embeddedEmissionMeter.record(embodiedEmissions, attributes);
    }

    /**
     * Publishes the calculated memory emissions.
     *
     * @param memoryEmission The total memory demand in bytes.
     * @param attributes     Additional attributes for the emission event.
     */
    public void publishMemoryEmissions(Double memoryEmission, Attributes attributes) {
        if (memoryEmission == null) {
            System.out.println("Memory emissions are null.");
            return;
        }
        memoryEmissionMeter.record(memoryEmission, attributes);
    }

    /**
     * Publishes emission metrics based on the provided attributes. It extracts values for
     * CPU, storage, embodied, and memory emissions from the attributes and delegates
     * publishing to specific methods.
     * If a value is missing or null, the corresponding method handles it appropriately.
     *
     * @param attributes An {@link Attributes} object containing emission data.
     */
    public void publishEmissions(Attributes attributes) {
        Double cpuEmissions = attributes.get(AttributeKey.doubleKey("cpuEmissionsInMg"));
        Double storageEmissions = attributes.get(AttributeKey.doubleKey("storageEmissionsInMg"));
        Double embodiedEmissions = attributes.get(AttributeKey.doubleKey("embodiedEmissionsInMg"));
        Double memoryEmissions = attributes.get(AttributeKey.doubleKey("memoryEmissionsInMg"));

        AttributesBuilder attributesBuilder = attributes.toBuilder();
        attributesBuilder.put(AttributeKey.stringKey("service-name"), configLoader.getServiceName());
        attributesBuilder.put(AttributeKey.stringKey("region"), configLoader.getRegion());
        attributesBuilder.put(AttributeKey.stringKey("instance-type"), configLoader.getCloudInstanceName());
        attributesBuilder.put(AttributeKey.stringKey("provider"), configLoader.getCloudProvider());
        Attributes finalAttributes = attributesBuilder.build();

        publishStorageEmissions(storageEmissions, attributes);
        publishCpuEmissions(cpuEmissions, attributes);
        publishEmbeddedEmissions(embodiedEmissions, attributes);
        publishMemoryEmissions(memoryEmissions, attributes);
    }
}

