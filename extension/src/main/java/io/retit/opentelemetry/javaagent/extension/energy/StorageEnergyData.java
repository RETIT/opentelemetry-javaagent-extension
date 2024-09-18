package io.retit.opentelemetry.javaagent.extension.energy;

import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

import java.util.logging.Logger;

/**
 * Wrapper class to calculate the energy in kWh required to store one GB on storage (e.g., HDD/SSD).
 */
public class StorageEnergyData {

    private static final Logger LOGGER = Logger.getLogger(StorageEnergyData.class.getName());

    private static final StorageEnergyData INSTANCE = new StorageEnergyData();

    private final double kwhPerGBMinute;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private StorageEnergyData() {
        // convert to kWh and then to one minute
        this.kwhPerGBMinute = (Constants.RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY_VALUE_SSD
                .equalsIgnoreCase(InstanceConfiguration.getStorageType())
                ? CloudCarbonFootprintCoefficients.STORAGE_ENERGY_CONSUMPTION_WH_SSD_PER_TB_HOUR / 1_000
                : CloudCarbonFootprintCoefficients.STORAGE_ENERGY_CONSUMPTION_WH_HDD_PER_TB_HOUR / 1_000) / 60.0;

        LOGGER.info("Initialized StorageEnergyData using following data: " + this);
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static StorageEnergyData getInstance() {
        return INSTANCE;
    }

    public double getKwhPerGBMinute() {
        return kwhPerGBMinute;
    }

    @Override
    public String toString() {
        return "StorageEnergyData{"
                + "kwhPerGBMinute=" + kwhPerGBMinute
                + '}';
    }
}
