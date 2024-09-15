package io.retit.opentelemetry.javaagent.extension.energy;

import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

import java.util.logging.Logger;

public class StorageEnergyData {

    private static final Logger LOGGER = Logger.getLogger(StorageEnergyData.class.getName());

    private static final StorageEnergyData instance = new StorageEnergyData();

    private final double kwhPerGBMinute;
    private final String instanceType;
    private final String cloudProvider;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private StorageEnergyData() {
        this.cloudProvider = InstanceConfiguration.getCloudProvider();
        this.instanceType = InstanceConfiguration.getCloudProviderInstanceType();
        // convert to kWh and then to one minute
        this.kwhPerGBMinute = (InstanceConfiguration.getStorageType()
                .equalsIgnoreCase(Constants.RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY_VALUE_SSD) ?
                CloudCarbonFootprintCoefficients.STORAGE_ENERGY_CONSUMPTION_WH_SSD_PER_TB_HOUR / 1_000 :
                CloudCarbonFootprintCoefficients.STORAGE_ENERGY_CONSUMPTION_WH_HDD_PER_TB_HOUR / 1_000) / 60.0;

        LOGGER.info("Initialized StorageEnergyData using following data: " + this);
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static StorageEnergyData getInstance() {
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
        return "StorageEnergyData{" +
                "kwhPerGBMinute=" + kwhPerGBMinute +
                ", instanceType='" + instanceType + '\'' +
                ", cloudProvider='" + cloudProvider + '\'' +
                '}';
    }
}
