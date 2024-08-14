package io.retit.opentelemetry.javaagent.extension.emissions.network;

import io.retit.opentelemetry.javaagent.extension.emissions.EmissionDataLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;

/**
 * The {@code NetworkEmission} class calculates the carbon emissions associated with network usage.
 * It leverages configuration settings to estimate emissions based on the amount of data transferred,
 * and the specific energy consumption characteristics of the cloud provider.
 *
 * <p>The approach and coefficients used in this class are derived
 * from the methodology outlined by the Cloud Carbon Footprint project, which provides a comprehensive framework
 * for calculating carbon emissions from cloud computing activities. More details can be found at:
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#network">Cloud Carbon Footprint Methodology</a>.
 */
public class NetworkEmission {

    private static NetworkEmission instance;
    private final EmissionDataLoader configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link EmissionDataLoader} to load necessary configuration for emissions calculations.
     */
    private NetworkEmission() {
        configLoader = EmissionDataLoader.getConfigInstance();
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
     * Calculates the carbon emissions in milligrams for a given amount of network data usage.
     * This method estimates emissions based on the amount of data transferred, applying the Power Usage Effectiveness (PUE)
     * and grid emissions factor to convert the energy consumption into CO2 emissions.
     *
     * <p>This approach follows the methodology outlined in the Cloud Carbon Footprint documentation:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#networking">Cloud Carbon Footprint Methodology</a>.
     *
     * @param amountInBytes The amount of network data used in bytes.
     * @return The calculated carbon emissions in milligrams.
     */
    public double calculateNetworkEmissionInMg(double amountInBytes) {
        return calculateKwhUsed(amountInBytes) * configLoader.getPueValue() * configLoader.getGridEmissionsFactor();
    }

    /**
     * Estimates the energy usage in kilowatt-hours based on the amount of network data used in bytes.
     * Converts the data amount from bytes to gigabytes and applies a fixed coefficient for network emissions.
     *
     * <p>This calculation is part of the overall carbon emissions estimation approach following the Cloud Carbon Footprint methodology:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#networking">Cloud Carbon Footprint Methodology</a>.
     *
     * @param amountInBytes The amount of network data used in bytes.
     * @return The estimated energy usage in kilowatt-hours.
     */
    public double calculateKwhUsed(double amountInBytes) {
        double dataSizeInGb = amountInBytes / (1024.0 * 1024.0 * 1024.0); // Convert bytes to gigabytes
        return dataSizeInGb * EmissionCoefficients.NETWORK_EMISSIONS_PER_GB_HOUR;
    }

}
