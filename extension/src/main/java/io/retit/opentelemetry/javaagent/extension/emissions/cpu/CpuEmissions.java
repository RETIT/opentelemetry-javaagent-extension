package io.retit.opentelemetry.javaagent.extension.emissions.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;

/**
 * The {@code CpuEmissions} class calculates the carbon emissions associated with CPU usage.
 * It utilizes configuration settings to estimate emissions based on the cloud provider's energy consumption data.
 * <p>
 * The approach and coefficients used in this class are derived from the methodology outlined by the
 * Cloud Carbon Footprint project, which provides a comprehensive framework for calculating carbon emissions
 * from cloud computing activities. More details can be found at:
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
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
     * <p>
     * The calculation uses a linear model based on the idle and full energy usage values provided by the cloud
     * provider. This method estimates the power consumption during the given CPU utilization and multiplies
     * it by the time the CPU was used to estimate the total energy consumed, which is then converted to carbon emissions.
     *
     * @param cpuTimeUsedInNanoSeconds    The CPU time used in nanoseconds.
     * @param cpuUtilization The CPU utilization as a percentage.
     * @return The calculated carbon emissions in grams.
     */
    public double calculateCpuEmissionsInMilliGram(double cpuTimeUsedInNanoSeconds, double cpuUtilization) {
        return configLoader.getInstanceEnergyUsageIdle() + cpuUtilization * (configLoader.getInstanceEnergyUsageFull()
                - configLoader.getInstanceEnergyUsageIdle()) / 1000 * cpuTimeUsedInNanoSeconds;
    }

    /**
     * Overloaded method to calculate carbon emissions based on CPU time used in nanoseconds.
     * <p>
     * This method simplifies the calculations by assuming a fixed CPU time used, calculating the energy consumption
     * based on idle and full energy usage, and then converting it into carbon emissions.
     *
     * @param cpuTimeUsedInNanoSeconds The CPU time used in nanoseconds.
     * @return The calculated carbon emissions in milligrams.
     */
    public double calculateCpuEmissionsInMilliGram(double cpuTimeUsedInNanoSeconds) {
        double cpuTimeInHours = cpuTimeUsedInNanoSeconds / 3600000.0 / 1000000;
        double computeKiloWattHours;
        double averageMinWatts = 0;
        double averageMaxWatts = 0;
        if (configLoader.getCloudInstanceName().equals("SERVERLESS")) {
            switch (configLoader.getCloudProvider()) {
                case "AWS":
                    averageMinWatts = EmissionCoefficients.AVERAGE_MIN_WATT_AWS;
                    averageMaxWatts = EmissionCoefficients.AVERAGE_MAX_WATT_AWS;
                    break;
                case "AZURE":
                    averageMinWatts = EmissionCoefficients.AVERAGE_MIN_WATT_AZURE;
                    averageMaxWatts = EmissionCoefficients.AVERAGE_MAX_WATT_AZURE;
                    break;
                case "GCP":
                    averageMinWatts = EmissionCoefficients.AVERAGE_MIN_WATT_GCP;
                    averageMaxWatts = EmissionCoefficients.AVERAGE_MAX_WATT_GCP;
                    break;
            }
            computeKiloWattHours = (averageMinWatts + 0.5 * (averageMaxWatts - averageMinWatts) * cpuTimeInHours) / 1000;
        } else {
            computeKiloWattHours = ((configLoader.getInstanceEnergyUsageIdle() + 0.5
                    * (configLoader.getInstanceEnergyUsageFull() - configLoader.getInstanceEnergyUsageIdle()))
                    * cpuTimeInHours) / 1000;
        }
        return computeKiloWattHours * configLoader.getPueValue() * configLoader.getGridEmissionsFactor() * 1000000;
    }
}
