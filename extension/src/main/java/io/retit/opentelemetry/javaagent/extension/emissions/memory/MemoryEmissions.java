package io.retit.opentelemetry.javaagent.extension.emissions.memory;

import io.retit.opentelemetry.javaagent.extension.emissions.EmissionDataLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;

/**
 * The {@code MemoryEmissions} class calculates the carbon emissions associated with memory usage.
 * It leverages configuration settings to estimate emissions based on the amount of memory used and
 * the specific energy consumption characteristics of the cloud provider.
 *
 * <p>The approach and coefficients used in this class are derived from the methodology outlined by the
 * Cloud Carbon Footprint project, which provides a comprehensive framework for calculating carbon emissions
 * from cloud computing activities. More details can be found at:
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
 */
public class MemoryEmissions {

    private static MemoryEmissions instance;
    EmissionDataLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link EmissionDataLoader} to load necessary configuration for emissions calculations.
     */
    private MemoryEmissions() {
        configLoader = EmissionDataLoader.getConfigInstance();
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
     * Calculates the carbon emissions in milligrams for a given amount of kilowatt-hours.
     * The method estimates emissions based on the memory usage, applying a fixed coefficient
     * and adjusting for the Power Usage Effectiveness (PUE) and the grid emissions factor.
     *
     * <p>This approach follows the methodology outlined in the Cloud Carbon Footprint documentation:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#memory">Cloud Carbon Footprint Methodology</a>.
     *
     * @param memoryKilowattHours The amount of memory used in bytes.
     * @return The calculated carbon emissions in milligrams.
     */
    public double calculateMemoryEmissionsInMilliGram(double memoryKilowattHours) {
        return memoryKilowattHours * configLoader.getPueValue() * configLoader.getGridEmissionsFactor() * 1000000;
    }

    /**
     * Estimates the energy usage in kilowatt-hours based on the amount of memory used in bytes.
     * It converts the memory amount to gigabytes and applies a fixed coefficient. We are assuming a fixxed usage time
     * of 60 seconds.
     *
     * <p>This calculation is part of the overall carbon emissions estimation approach following the Cloud Carbon Footprint methodology:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#memory">Cloud Carbon Footprint Methodology</a>.
     *
     * @param amountInBytes The amount of memory used in bytes.
     * @return The estimated energy usage in kilowatt-hours.
     */
    public double energyUsageInKiloWattHours(double amountInBytes) {
        double amountInGb = amountInBytes / (1024.0 * 1024.0 * 1024.0);
        double emissionsCoefficientPer60Sec = EmissionCoefficients.MEMORY_KWH_PER_GB_HOUR / 60.0;
        return amountInGb * emissionsCoefficientPer60Sec;
    }
}