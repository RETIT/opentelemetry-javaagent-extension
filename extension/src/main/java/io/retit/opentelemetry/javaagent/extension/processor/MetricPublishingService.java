package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.common.Attributes;

/**
 * Handles the publishing of various emissions metrics to an OpenTelemetry meter.
 * This service is responsible for calculating and publishing emissions from storage, CPU, embedded components, and memory.
 */
public class MetricPublishingService {

    private static MetricPublishingService instance;

    private final DoubleHistogram storageEmissionMeter;
    private final DoubleHistogram cpuEmissionMeter;
    private final DoubleHistogram embeddedEmissionMeter;
    private final DoubleHistogram memoryEmissionMeter;

    private final DoubleHistogram cpuEnergyUsageMeter;
    private final DoubleHistogram memoryEnergyUsageMeter;
    private final DoubleHistogram storageEnergyUsageMeter;

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

        cpuEnergyUsageMeter = meter.histogramBuilder("cpu_energy_usage")
                .setDescription("Total Watt usage from CPU")
                .setUnit("W")
                .build();

        memoryEnergyUsageMeter = meter.histogramBuilder("memory_energy_usage")
                .setDescription("Total Watt usage from memory")
                .setUnit("W")
                .build();

        storageEnergyUsageMeter = meter.histogramBuilder("storage_energy_usage")
                .setDescription("Total Watt usage from storage")
                .setUnit("W")
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
            return;
        }
        memoryEmissionMeter.record(memoryEmission, attributes);
    }

    /**
     * Publishes the calculated CPU energy usage.
     *
     * @param cpuEnergyUsage The total CPU energy usage in Watt-hours.
     * @param attributes     Additional attributes for the energy usage event.
     */
    public void publishCpuEnergyUsage(Double cpuEnergyUsage, Attributes attributes) {
        if (cpuEnergyUsage == null) {
            return;
        }
        cpuEnergyUsageMeter.record(cpuEnergyUsage, attributes);
    }

    /**
     * Publishes the calculated memory energy usage.
     *
     * @param memoryEnergyUsage The total memory energy usage in Watt-hours.
     * @param attributes        Additional attributes for the energy usage event.
     */
    public void publishMemoryEnergyUsage(Double memoryEnergyUsage, Attributes attributes) {
        if (memoryEnergyUsage == null) {
            return;
        }
        memoryEnergyUsageMeter.record(memoryEnergyUsage, attributes);
    }

    /**
     * Publishes the calculated storage energy usage.
     *
     * @param storageEnergyUsage The total storage energy usage in Watt-hours.
     * @param attributes         Additional attributes for the energy usage event.
     */
    public void publishStorageEnergyUsage(Double storageEnergyUsage, Attributes attributes) {
        if (storageEnergyUsage == null) {
            return;
        }
        storageEnergyUsageMeter.record(storageEnergyUsage, attributes);
    }

    /**
     * Publishes emission metrics based on the provided attributes. It extracts values for
     * CPU, storage, embodied, and memory emissions from the attributes and delegates
     * publishing to specific methods.
     * If a value is missing or null, the corresponding method handles it appropriately.
     *
     * @param attributes An {@link Attributes} object containing the route of the http request.
     * @param servicecall An {@link Attributes} object containing the service call.
     */
    public void publishEmissions(Attributes attributes, Attributes servicecall) {

   /*     AttributesBuilder attributesBuilder = Attributes.builder().putAll(attributes);
        attributesBuilder.put(AttributeKey.stringKey("service-name"), configLoader.getServiceName());
        attributesBuilder.put(AttributeKey.stringKey("region"), configLoader.getRegion());
        attributesBuilder.put(AttributeKey.stringKey("instance-type"), configLoader.getCloudInstanceName());
        attributesBuilder.put(AttributeKey.stringKey("provider"), configLoader.getCloudProvider());
        attributesBuilder.put(AttributeKey.stringKey("Servicecall"), servicecall.get(AttributeKey.stringKey("Servicecall")));
        Attributes finalAttributes = attributesBuilder.build();*/

        Double cpuEmissions = attributes.get(AttributeKey.doubleKey("cpuEmissionsInMg"));
        Double memoryEmissions = attributes.get(AttributeKey.doubleKey("memoryEmissionsInMg"));
        Double storageEmissions = attributes.get(AttributeKey.doubleKey("storageEmissionsInMg"));
        Double embodiedEmissions = attributes.get(AttributeKey.doubleKey("embodiedEmissionsInMg"));

        publishStorageEmissions(storageEmissions, servicecall);
        publishCpuEmissions(cpuEmissions, servicecall);
        publishEmbeddedEmissions(embodiedEmissions, servicecall);
        publishMemoryEmissions(memoryEmissions, servicecall);
    }

    public void publishWattHoursUsage(Attributes attributes, Attributes servicecall) {

        Double cpuEnergyUsage = attributes.get(AttributeKey.doubleKey("cpuWattHoursUsage"));
        Double memoryEnergyUsage = attributes.get(AttributeKey.doubleKey("memoryKwhUsed"));
        Double storageEnergyUsage = attributes.get(AttributeKey.doubleKey("storageKwhUsed"));

        publishCpuEnergyUsage(cpuEnergyUsage, servicecall);
        publishMemoryEnergyUsage(memoryEnergyUsage, servicecall);
        publishStorageEnergyUsage(storageEnergyUsage, servicecall);
    }
}

