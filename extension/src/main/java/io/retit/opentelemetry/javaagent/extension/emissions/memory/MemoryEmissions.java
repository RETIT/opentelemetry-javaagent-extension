package io.retit.opentelemetry.javaagent.extension.emissions.memory;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

/**
 * The {@code MemoryEmissions} class calculates the carbon emissions associated with memory usage.
 * It leverages configuration settings to estimate emissions based on the amount of memory used and
 * the specific energy consumption characteristics of the cloud provider.
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
    public double calculateMemoryEmissionsInGramm(double amountInBytes) {
        // Simplification for now, assuming a fixed amount of memory usage
        amountInBytes = 10000; // Simplification to get easier numbers
        amountInBytes *= 0.000392 * configLoader.getPueValue() * configLoader.getGridEmissionsFactor(); // 0.000392 is a fixed coefficient
        return amountInBytes;
    }
}