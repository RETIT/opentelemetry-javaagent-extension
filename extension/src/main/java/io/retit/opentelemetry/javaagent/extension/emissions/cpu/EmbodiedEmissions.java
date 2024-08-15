package io.retit.opentelemetry.javaagent.extension.emissions.cpu;

import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionDataLoader;

/**
 * The {@code EmbodiedEmissions} class calculates the embodied carbon emissions associated with the usage of CPU resources.
 * Embodied emissions refer to the carbon footprint incurred during the manufacturing and provisioning of computing resources.
 */
public class EmbodiedEmissions {

    private static EmbodiedEmissions instance;
    private final EmissionDataLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link EmissionDataLoader} to load necessary configuration for emissions calculations.
     */
    private EmbodiedEmissions() {
        configLoader = EmissionDataLoader.getConfigInstance();
    }

    /**
     * Provides a global access point to the {@code EmbodiedEmissions} instance, implementing a singleton pattern.
     * This ensures that only one instance of the class is created and used throughout the application.
     *
     * @return The single instance of {@code EmbodiedEmissions}.
     */
    public static EmbodiedEmissions getInstance() {
        if (instance == null) {
            instance = new EmbodiedEmissions();
        }
        return instance;
    }

    /**
     * Calculates the embodied carbon emissions in grams for a given CPU usage time in hours.
     * This method estimates the emissions based on the cloud instance's specifications and the total embodied emissions data.
     *
     * @param cpuTimeUsedInNanoSeconds The CPU time used in hours.
     * @return The calculated embodied carbon emissions in milligrams.
     */
    public double calculateEmbodiedEmissionsInMilliGram(final double cpuTimeUsedInNanoSeconds) {
        if (InstanceConfiguration.getCloudProviderInstanceType() == null || Constants.RETIT_VALUE_NOT_SET.equals(InstanceConfiguration.getCloudProviderInstanceType())) {
            return 0;
        } else {
            double cpuTimeInHours = cpuTimeUsedInNanoSeconds / 3600000.0 / 1000000;
            return (configLoader.getTotalEmbodiedEmissions() * EmissionCoefficients.TOTAL_EMBODIED_EMISSIONS_TO_GRAMS_PER_HOUR
                    * (configLoader.getInstanceVCpu() / configLoader.getPlatformTotalVcpu()) * cpuTimeInHours) * 1000;
        }
    }
}