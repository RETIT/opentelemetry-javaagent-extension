package io.retit.opentelemetry.javaagent.extension.emissions.storage;

import io.retit.opentelemetry.javaagent.extension.emissions.EmissionDataLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;

/**
 * The {@code StorageEmissions} class calculates the carbon emissions associated with storage device usage.
 * It leverages configuration settings to estimate emissions based on the type of storage (HDD or SSD),
 * the amount of data stored, and the specific energy consumption characteristics of the cloud provider.
 * The approach and coefficients used in this class are derived from the methodology outlined by the
 * Cloud Carbon Footprint project, which provides a comprehensive framework for calculating carbon emissions
 * from cloud computing activities. More details can be found at:
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
 */
public class StorageEmissions {

    private static StorageEmissions instance;
    private final EmissionDataLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link EmissionDataLoader} to load necessary configuration for emissions calculations.
     */
    private StorageEmissions() {
        configLoader = EmissionDataLoader.getConfigInstance();
    }

    /**
     * Provides a global access point to the {@code StorageEmissions} instance, implementing a singleton pattern.
     * This ensures that only one instance of the class is created and used throughout the application.
     *
     * @return The single instance of {@code StorageEmissions}.
     */
    public static StorageEmissions getInstance() {
        if (instance == null) {
            instance = new StorageEmissions();
        }
        return instance;
    }

    /**
     * Calculates the carbon emissions in milligrams for a given amount of kilowatt-hours.
     * The method estimates emissions based on the storage usage, applying a fixed coefficient
     * and adjusting for the Power Usage Effectiveness (PUE) and the grid emissions factor.
     *
     * <p>This approach follows the methodology outlined in the Cloud Carbon Footprint documentation:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#storage">Cloud Carbon Footprint Methodology</a>.
     *
     * @param storageKilowattHours The amount of storage used in bytes.
     * @return The calculated carbon emissions in milligrams.
     */
    public double calculateStorageEmissionsInMilliGram(double storageKilowattHours) {
        return storageKilowattHours * configLoader.getPueValue() * configLoader.getGridEmissionsFactor() * 1000000;
    }

    /**
     * Estimates the energy usage in kilowatt-hours based on the amount of storage used in bytes.
     * It converts the storage amount to terabytes and applies a fixed coefficient depending on the storage type,
     * adjusting for a 60-second interval.
     *
     * <p>This calculation is part of the overall carbon emissions estimation approach following the Cloud Carbon Footprint methodology:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
     *
     * @param amountInBytes The amount of storage used in bytes.
     * @return The estimated energy usage in kilowatt-hours.
     */
    public double energyUsageInKiloWattHours(double amountInBytes) {
        double storageSizeInTB = amountInBytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);

        double emissionsCoefficientPer60Sec = (configLoader.getStorageType().equalsIgnoreCase("SSD")
                ? EmissionCoefficients.STORAGE_EMISSIONS_SSD_PER_TB_HOUR
                : EmissionCoefficients.STORAGE_EMISSIONS_HDD_PER_TB_HOUR) / 60.0;

        return storageSizeInTB * emissionsCoefficientPer60Sec;
    }
}