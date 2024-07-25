package io.retit.opentelemetry.javaagent.extension.emissions.storage;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

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

    public static StorageEmissions instance;
    public static final double STORAGE_EMISSIONS_HDD_PER_TB_HOUR = 0.00065;
    public static final double STORAGE_EMISSIONS_SSD_PER_TB_HOUR = 0.0012;
    private final ConfigLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link ConfigLoader} to load necessary configuration for emissions calculations.
     */
    private StorageEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
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
     * Calculates the carbon emissions in grams for a given amount of storage usage.
     * This method estimates the emissions based on the storage type (HDD or SSD), the amount of data stored,
     * and adjusts for the Power Usage Effectiveness (PUE) value and the grid emissions factor.
     *
     * @param amountInBytes The amount of storage used in bytes.
     * @return The calculated carbon emissions in grams.
     */
    public double calculateStorageEmissionsInMilliGram(double amountInBytes) {
        double storageSize = amountInBytes / 1024 / 1024 / 1024 / 1024; // Convert bytes to terabytes
        if (configLoader.getStorageType().equals("HDD")) {
            storageSize *= STORAGE_EMISSIONS_HDD_PER_TB_HOUR * 1000000;
        } else {
            storageSize *= STORAGE_EMISSIONS_SSD_PER_TB_HOUR * 1000000;
        }
        storageSize *= configLoader.getPueValue() * configLoader.getGridEmissionsFactor();
        return storageSize;
    }
}