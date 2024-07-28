package io.retit.opentelemetry.javaagent.extension.emissions.network;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;

/**
 * The {@code NetworkEmission} class calculates the carbon emissions associated with network usage.
 * It leverages configuration settings to estimate emissions based on the amount of data transferred,
 * and the specific energy consumption characteristics of the cloud provider.
 * The approach and coefficients used in this class are derived
 * from the methodology outlined by the Cloud Carbon Footprint project, which provides a comprehensive framework
 * for calculating carbon emissions from cloud computing activities. More details can be found at:
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#network">Cloud Carbon Footprint Methodology</a>.
 */
public class NetworkEmission {

    public static NetworkEmission instance;
    private final ConfigLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link ConfigLoader} to load necessary configuration for emissions calculations.
     */
    private NetworkEmission() {
        configLoader = ConfigLoader.getConfigInstance();
    }

    /**
     * Provides a global access point to the {@code StorageEmissions} instance, implementing a singleton pattern.
     * This ensures that only one instance of the class is created and used throughout the application.
     *
     * @return The single instance of {@code StorageEmissions}.
     */
    public static NetworkEmission getInstance() {
        if (instance == null) {
            instance = new NetworkEmission();
        }
        return instance;
    }

    /**
     * Calculates the carbon emissions in grams for a given amount of storage usage.
     * This method estimates the emissions based on the storage type (HDD or SSD), the amount of data stored,
     * and adjusts for the Power Usage Effectiveness (PUE) value and the grid emissions factor.
     *
     * @param amountInBytes The amount of storage used in bytes.
     * @return The calculated carbon emissions in milligrams.
     */
    public double calculateNetworkEmissionInMg(double amountInBytes) {
        double storageSize = amountInBytes / 1024 / 1024 / 1024; // Convert bytes to gigabytes
        storageSize *= EmissionCoefficients.NETWORK_EMISSIONS_PER_GB * configLoader.getPueValue() * configLoader.getGridEmissionsFactor();
        return storageSize;
    }
}
