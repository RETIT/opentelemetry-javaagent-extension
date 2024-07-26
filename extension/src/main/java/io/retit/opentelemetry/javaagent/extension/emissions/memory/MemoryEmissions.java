package io.retit.opentelemetry.javaagent.extension.emissions.memory;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;

/**
 * The {@code MemoryEmissions} class calculates the carbon emissions associated with memory usage.
 * It leverages configuration settings to estimate emissions based on the amount of memory used and
 * the specific energy consumption characteristics of the cloud provider.
 * The approach and coefficients used in this class are derived from the methodology outlined by the
 * Cloud Carbon Footprint project, which provides a comprehensive framework for calculating carbon emissions
 * from cloud computing activities. More details can be found at:
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
 */
public class MemoryEmissions {
    public static MemoryEmissions instance;
    ConfigLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link ConfigLoader} to load necessary configuration for emissions calculations.
     */
    private MemoryEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
    }

    /**
     * Provides a global access point to the {@code MemoryEmissions} instance, implementing a singleton pattern.
     * This ensures that only one instance of the class is created and used throughout the application.
     *
     * @return The single instance of {@code MemoryEmissions}.
     */
    public static MemoryEmissions getInstance() {
        if (instance == null) {
            instance = new MemoryEmissions();
        }
        return instance;
    }

    /**
     * Calculates the carbon emissions in grams for a given amount of memory usage.
     * This method estimates the emissions based on the amount of memory used, applying a fixed coefficient
     * and adjusting for the Power Usage Effectiveness (PUE) value and the grid emissions factor.
     *
     * @param amountInBytes The amount of memory used in bytes.
     * @return The calculated carbon emissions in grams.
     */
    public double calculateMemoryEmissionsInMilliGram(double amountInBytes) {
        double amountInGb = amountInBytes / 1024 / 1024 / 1024; // Convert bytes to gigabytes
        return amountInGb * EmissionCoefficients.MEMORY_KWH_PER_GB * configLoader.getPueValue() * configLoader.getGridEmissionsFactor() * 1000000;
    }
}