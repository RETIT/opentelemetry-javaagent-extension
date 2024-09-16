package io.retit.opentelemetry.javaagent.extension.energy;

import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

import java.util.logging.Logger;

/**
 * Wrapper class to calculate the energy in kWh required to process one GB over the network.
 */
public class NetworkEnergyData {

    private static final Logger LOGGER = Logger.getLogger(NetworkEnergyData.class.getName());

    private static final NetworkEnergyData INSTANCE = new NetworkEnergyData();

    private final double kwhPerGBMinute;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private NetworkEnergyData() {
        // convert to kWh and then to one minute

        this.kwhPerGBMinute = CloudCarbonFootprintCoefficients.NETWORK_KWH_PER_GB_HOUR / 60;

        LOGGER.info("Initialized NetworkEnergyData using following data: " + this);
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static NetworkEnergyData getInstance() {
        return INSTANCE;
    }

    public double getKwhPerGBMinute() {
        return kwhPerGBMinute;
    }

    @Override
    public String toString() {
        return "NetworkEnergyData{"
                + "kwhPerGBMinute=" + kwhPerGBMinute
                + '}';
    }
}
