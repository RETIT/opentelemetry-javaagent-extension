package io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

import static io.retit.opentelemetry.javaagent.extension.config.CloudProvider.AWS;

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
     * @param cpuTimeUsed The CPU time used in nanoseconds.
     * @param cpuUtilization The CPU utilization as a percentage.
     * @return The calculated carbon emissions in grams.
     */
    public double calculateCpuEmissionsInGramm(double cpuTimeUsed, double cpuUtilization) {
        return configLoader.getInstanceEnergyUsageIdle() + cpuUtilization * (configLoader.getInstanceEnergyUsageFull()
                - configLoader.getInstanceEnergyUsageIdle()) / 1000 * cpuTimeUsed;
    }

    /**
     * Overloaded method to calculate carbon emissions based on CPU time used in hours.
     * This method simplifies calculations by assuming a fixed CPU time used.
     *
     * @param cpuTimeUsedinHours The CPU time used in hours.
     * @return The calculated carbon emissions in grams.
     */
    public double calculateCpuEmissionsInGramm(double cpuTimeUsedinHours) {
        cpuTimeUsedinHours = 10000; // Simplification for now
        if (configLoader.getCloudInstanceName() == null && configLoader.getCloudProvider().equals(AWS)) {
            return (0.74 + 0.5 * (0.74 - 3.5) * cpuTimeUsedinHours * configLoader.getPueValue()
                    - configLoader.getInstanceEnergyUsageIdle() / 1000 * cpuTimeUsedinHours * configLoader.getPueValue()
                    * configLoader.getGridEmissionsFactor());
        } else {
            return configLoader.getInstanceEnergyUsageIdle() + 0.5 * (configLoader.getInstanceEnergyUsageFull()
                    - configLoader.getInstanceEnergyUsageIdle()) / 1000 * cpuTimeUsedinHours * configLoader.getPueValue()
                    * configLoader.getGridEmissionsFactor();
        }
    }
}