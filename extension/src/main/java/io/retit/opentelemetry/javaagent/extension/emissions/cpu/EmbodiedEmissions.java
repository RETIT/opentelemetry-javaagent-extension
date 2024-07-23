package io.retit.opentelemetry.javaagent.extension.emissions.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

/**
 * The {@code EmbodiedEmissions} class calculates the embodied carbon emissions associated with the usage of CPU resources.
 * Embodied emissions refer to the carbon footprint incurred during the manufacturing and provisioning of computing resources.
 */
public class EmbodiedEmissions {

    private static EmbodiedEmissions instance;
    private final ConfigLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link ConfigLoader} to load necessary configuration for emissions calculations.
     */
    private EmbodiedEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
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
     * @param cpuTimeUsedInMilliSeconds The CPU time used in hours.
     * @return The calculated embodied carbon emissions in grams.
     */
    public double calculateEmbodiedEmissionsInMilliGram(double cpuTimeUsedInMilliSeconds) {
        if (configLoader.getCloudInstanceName() == null) {
            return 0;
        } else {
            return (configLoader.getTotalEmbodiedEmissions() * 0.0289 * (configLoader.getInstanceVCpu() /
                    configLoader.getPlatformTotalVcpu()) * cpuTimeUsedInMilliSeconds / 3600000) * 1000000;
        }
    }
}