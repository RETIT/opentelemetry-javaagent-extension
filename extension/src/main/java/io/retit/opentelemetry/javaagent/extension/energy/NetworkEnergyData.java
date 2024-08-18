package io.retit.opentelemetry.javaagent.extension.energy;

import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

import java.util.logging.Logger;

public class NetworkEnergyData {

    private static final Logger LOGGER = Logger.getLogger(NetworkEnergyData.class.getName());

    private static NetworkEnergyData instance;

    private final double kwhPerGBMinute;
    private final String instanceType;
    private final String cloudProvider;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private NetworkEnergyData() {
        this.cloudProvider = InstanceConfiguration.getCloudProvider();
        this.instanceType = InstanceConfiguration.getCloudProviderInstanceType();
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
        if (instance == null) {
            instance = new NetworkEnergyData();
        }
        return instance;
    }

    public double getKwhPerGBMinute() {
        return kwhPerGBMinute;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    @Override
    public String toString() {
        return "NetworkEnergyData{" +
                "kwhPerGBMinute=" + kwhPerGBMinute +
                ", instanceType='" + instanceType + '\'' +
                ", cloudProvider='" + cloudProvider + '\'' +
                '}';
    }
}
