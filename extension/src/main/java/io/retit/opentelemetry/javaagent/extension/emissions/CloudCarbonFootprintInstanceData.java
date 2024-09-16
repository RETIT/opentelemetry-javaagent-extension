package io.retit.opentelemetry.javaagent.extension.emissions;

/**
 * This class holds all the configuration data for a given instance.
 */
public class CloudCarbonFootprintInstanceData {
    // cloud provider of the instance
    private CloudProvider cloudProvider;
    // instance type
    private String instanceType;
    // Number of Instance vCPUs
    private double instanceVCpuCount;
    // Number of Platform Total vCPU
    private double platformTotalVCpuCount;
    // Instance Watt usage @ Idle
    private double cpuPowerConsumptionIdle;
    // Instance Watt usage @ 100%
    private double cpuPowerConsumption100Percent;
    // the total embodied emissions of the server running this vminstance
    private double totalEmbodiedEmissions;

    /**
     * Default constructor.
     */
    public CloudCarbonFootprintInstanceData() {
    }

    /**
     * Constructor when data is already available.
     *
     * @param instanceVCpuCount             - Number of Instance vCPUs
     * @param platformTotalVCpuCount        - Number of Platform total vCPUs
     * @param cpuPowerConsumptionIdle       - Instance Watt usage @ Idle
     * @param cpuPowerConsumption100Percent - Instance Watt usage @ 100% Utilization
     * @param totalEmbodiedEmissions        - the total embodied emissions of the server running this vminstance
     */
    public CloudCarbonFootprintInstanceData(final double instanceVCpuCount,
                                            final double platformTotalVCpuCount,
                                            final double cpuPowerConsumptionIdle,
                                            final double cpuPowerConsumption100Percent,
                                            final double totalEmbodiedEmissions) {
        this.instanceVCpuCount = instanceVCpuCount;
        this.platformTotalVCpuCount = platformTotalVCpuCount;
        this.cpuPowerConsumptionIdle = cpuPowerConsumptionIdle;
        this.cpuPowerConsumption100Percent = cpuPowerConsumption100Percent;
        this.totalEmbodiedEmissions = totalEmbodiedEmissions;
    }

    public double getInstanceVCpuCount() {
        return instanceVCpuCount;
    }

    public void setInstanceVCpuCount(final double instanceVCpuCount) {
        this.instanceVCpuCount = instanceVCpuCount;
    }

    public double getPlatformTotalVCpuCount() {
        return platformTotalVCpuCount;
    }

    public void setPlatformTotalVCpuCount(final double platformTotalVCpuCount) {
        this.platformTotalVCpuCount = platformTotalVCpuCount;
    }

    public double getCpuPowerConsumptionIdle() {
        return cpuPowerConsumptionIdle;
    }

    public void setCpuPowerConsumptionIdle(final double cpuPowerConsumptionIdle) {
        this.cpuPowerConsumptionIdle = cpuPowerConsumptionIdle;
    }

    public double getCpuPowerConsumption100Percent() {
        return cpuPowerConsumption100Percent;
    }

    public void setCpuPowerConsumption100Percent(final double cpuPowerConsumption100Percent) {
        this.cpuPowerConsumption100Percent = cpuPowerConsumption100Percent;
    }

    public double getTotalEmbodiedEmissions() {
        return totalEmbodiedEmissions;
    }

    public void setTotalEmbodiedEmissions(final double totalEmbodiedEmissions) {
        this.totalEmbodiedEmissions = totalEmbodiedEmissions;
    }

    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(final CloudProvider cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(final String instanceType) {
        this.instanceType = instanceType;
    }
}
