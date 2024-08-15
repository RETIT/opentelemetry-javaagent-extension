package io.retit.opentelemetry.javaagent.extension.emissions.cpu;

import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.EmissionDataLoader;

/**
 * The {@code CpuEmissions} class calculates the carbon emissions associated with CPU usage.
 * It utilizes configuration settings to estimate emissions based on the cloud provider's energy consumption data.
 *
 * <p>The approach and coefficients used in this class are derived from the methodology outlined by the
 * Cloud Carbon Footprint project, which provides a comprehensive framework for calculating carbon emissions
 * from cloud computing activities. More details can be found at:
 * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
 */
public class CpuEmissions {

    private static CpuEmissions instance;
    private final EmissionDataLoader configLoader;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link EmissionDataLoader} to load necessary configuration.
     */
    private CpuEmissions() {
        configLoader = EmissionDataLoader.getConfigInstance();
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
     * Calculates the CO2 emissions in milligrams based on the given compute kilowatt-hours.
     * This method uses the Power Usage Effectiveness (PUE) and grid emissions factor to convert
     * energy consumption into CO2 emissions.
     *
     * <p>This approach follows the methodology outlined in the Cloud Carbon Footprint documentation:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
     *
     * @param computeKiloWattHours The energy consumption in kilowatt-hours.
     * @return The estimated CO2 emissions in milligrams.
     */
    public double calculateCpuEmissionsInMilliGram(double computeKiloWattHours) {
        return computeKiloWattHours * configLoader.getPueValue() * configLoader.getGridEmissionsFactor() * 1000000;
    }

    /**
     * Calculates the total energy consumption in kilowatt-hours based on CPU time used in nanoseconds.
     * It considers whether the service is serverless and applies different energy usage factors accordingly.
     * This method is part of the CO2 emissions calculation approach following the Cloud Carbon Footprint methodology:
     * <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">Cloud Carbon Footprint Methodology</a>.
     *
     * @param cpuTimeUsedInNanoSeconds The total CPU time used in nanoseconds.
     * @return The total energy consumption in kilowatt-hours.
     */
    public double calculateKwhUsed(double cpuTimeUsedInNanoSeconds) {
        double cpuTimeInHours = cpuTimeUsedInNanoSeconds / 3600000.0 / 1000000;
        double computeKiloWattHours;
        double averageMinWatts = 0;
        double averageMaxWatts = 0;
        if (InstanceConfiguration.getCloudProviderInstanceType().equals("SERVERLESS")) {
            switch (InstanceConfiguration.getCloudProvider()) {
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
            computeKiloWattHours = (averageMinWatts + configLoader.getCpuUtilization() * (averageMaxWatts - averageMinWatts) * cpuTimeInHours) / 1000 * EmissionDataLoader.getConfigInstance().getCpuCount();
        } else {
            computeKiloWattHours = ((configLoader.getInstanceEnergyUsageIdle() + configLoader.getCpuUtilization()
                    * (configLoader.getInstanceEnergyUsageFull() - configLoader.getInstanceEnergyUsageIdle()))
                    * cpuTimeInHours) / 1000;
        }
        return computeKiloWattHours;
    }
}
