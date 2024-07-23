package io.retit.opentelemetry.javaagent.extension.emissions.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

/**
 * The {@code CpuEmissions} class calculates the carbon emissions associated with CPU usage.
 * It utilizes configuration settings to estimate emissions based on the cloud provider's energy consumption data.
 */
public class CpuEmissions {

    private static CpuEmissions instance;
    private final ConfigLoader configLoader;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link ConfigLoader} to load necessary configuration.
     */
    private CpuEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static CpuEmissions getInstance() {
        if (instance == null) {
            instance = new CpuEmissions();
        }
        return instance;
    }

    /**
     * Calculates the carbon emissions in grams for a given CPU usage time and utilization.
     *
     * @param cpuTimeUsed    The CPU time used in nanoseconds.
     * @param cpuUtilization The CPU utilization as a percentage.
     * @return The calculated carbon emissions in grams.
     */
    public double calculateCpuEmissionsInMilliGram(double cpuTimeUsed, double cpuUtilization) {
        return configLoader.getInstanceEnergyUsageIdle() + cpuUtilization * (configLoader.getInstanceEnergyUsageFull()
                - configLoader.getInstanceEnergyUsageIdle()) / 1000 * cpuTimeUsed;
    }

    /**
     * Overloaded method to calculate carbon emissions based on CPU time used in hours.
     * This method simplifies calculations by assuming a fixed CPU time used.
     *
     * @param cpuTimeUsedinMilliseconds The CPU time used in hours.
     * @return The calculated carbon emissions in milligrams.
     */
    public double calculateCpuEmissionsInMilliGram(double cpuTimeUsedinMilliseconds) {
        double cpuTimeInHours = cpuTimeUsedinMilliseconds / 3600000.0 / 1000000;
        double computeKiloWattHours = (configLoader.getInstanceEnergyUsageIdle() + 0.5
                * (configLoader.getInstanceEnergyUsageFull() - configLoader.getInstanceEnergyUsageIdle())
                * cpuTimeInHours) / 1000;
        return computeKiloWattHours * configLoader.getPueValue() * configLoader.getGridEmissionsFactor() * 1000000;
    }
}