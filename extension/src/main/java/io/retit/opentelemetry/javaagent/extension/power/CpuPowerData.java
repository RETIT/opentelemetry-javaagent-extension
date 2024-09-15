package io.retit.opentelemetry.javaagent.extension.power;

import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

import java.util.logging.Logger;

public class CpuPowerData {

    private static final Logger LOGGER = Logger.getLogger(CpuPowerData.class.getName());

    private static final CpuPowerData INSTANCE = new CpuPowerData();

    private final double minPowerCPU;
    private final double maxPowerCPU;
    private final String instanceType;
    private final String cloudProvider;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private CpuPowerData() {
        this.cloudProvider = InstanceConfiguration.getCloudProvider();
        this.instanceType = InstanceConfiguration.getCloudProviderInstanceType();

        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_SERVERLESS_VALUE.equals(InstanceConfiguration.getCloudProviderInstanceType())) {
            switch (InstanceConfiguration.getCloudProvider()) {
                case Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS:
                    this.minPowerCPU = CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AWS;
                    this.maxPowerCPU = CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AWS;
                    break;
                case Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE:
                    this.minPowerCPU = CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AZURE;
                    this.maxPowerCPU = CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AZURE;
                    break;
                case Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP:
                    this.minPowerCPU = CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_GCP;
                    this.maxPowerCPU = CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_GCP;
                    break;
                default:
                    this.minPowerCPU = 0.0;
                    this.maxPowerCPU = 0.0;
            }
        } else {
            this.minPowerCPU = CloudCarbonFootprintData.getConfigInstance().getCloudInstanceDetails().getInstanceEnergyUsageIdle();
            this.maxPowerCPU = CloudCarbonFootprintData.getConfigInstance().getCloudInstanceDetails().getInstanceEnergyUsageFull();
        }

        LOGGER.info("Initialized CpuPowerData using following data: " + this);
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static CpuPowerData getInstance() {
        return INSTANCE;
    }

    public double getMinPowerCPU() {
        return minPowerCPU;
    }

    public double getMaxPowerCPU() {
        return maxPowerCPU;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    @Override
    public String toString() {
        return "CpuPowerData{" + "minPowerCPU=" + minPowerCPU + ", maxPowerCPU=" + maxPowerCPU + ", instanceType='" + instanceType + '\'' + ", cloudProvider='" + cloudProvider + '\'' + '}';
    }
}
