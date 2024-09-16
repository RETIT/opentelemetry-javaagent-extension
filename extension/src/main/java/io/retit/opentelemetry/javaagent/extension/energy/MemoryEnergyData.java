package io.retit.opentelemetry.javaagent.extension.energy;

import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

import java.util.logging.Logger;

public class MemoryEnergyData {

    private static final Logger LOGGER = Logger.getLogger(MemoryEnergyData.class.getName());

    private static final MemoryEnergyData INSTANCE = new MemoryEnergyData();

    private final double kwhPerGBMinute;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private MemoryEnergyData() {
        // convert to one minute
        this.kwhPerGBMinute = CloudCarbonFootprintCoefficients.MEMORY_KWH_PER_GB_HOUR / 60;

        LOGGER.info("Initialized MemoryEnergyData using following data: " + this);
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static MemoryEnergyData getInstance() {
        return INSTANCE;
    }

    public double getKwhPerGBMinute() {
        return kwhPerGBMinute;
    }

    @Override
    public String toString() {
        return "MemoryEnergyData{" +
                "kwhPerGBMinute=" + kwhPerGBMinute +
                '}';
    }
}
