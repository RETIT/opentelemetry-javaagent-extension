package io.retit.opentelemetry.javaagent.extension.emissions;

public class CloudCarbonFootprintInstanceData {
    // cloud provider of the instance
    private CloudProvider cloudProvider;
    // instance type
    private String instanceType;
    // Number of Instance vCPU
    private double instanceVCpuCount;
    // Number of Platform Total vCPU
    private double platformTotalVcpu;
    // Instance Watt usage @ Idle
    private double instanceEnergyUsageIdle;
    // Instance Watt usage @ 100%
    private double instanceEnergyUsageFull;
    // the total embodied emissions of the server running this vminstance
    private double totalEmbodiedEmissions;

    public CloudCarbonFootprintInstanceData() {
    }

    public CloudCarbonFootprintInstanceData(final double instanceVCpuCount,
                                            final double platformTotalVcpu,
                                            final double instanceEnergyUsageIdle,
                                            final double instanceEnergyUsageFull,
                                            final double totalEmbodiedEmissions) {
        this.instanceVCpuCount = instanceVCpuCount;
        this.platformTotalVcpu = platformTotalVcpu;
        this.instanceEnergyUsageIdle = instanceEnergyUsageIdle;
        this.instanceEnergyUsageFull = instanceEnergyUsageFull;
        this.totalEmbodiedEmissions = totalEmbodiedEmissions;
    }

    public double getInstanceVCpuCount() {
        return instanceVCpuCount;
    }

    public void setInstanceVCpuCount(final double instanceVCpuCount) {
        this.instanceVCpuCount = instanceVCpuCount;
    }

    public double getPlatformTotalVcpu() {
        return platformTotalVcpu;
    }

    public void setPlatformTotalVcpu(final double platformTotalVcpu) {
        this.platformTotalVcpu = platformTotalVcpu;
    }

    public double getInstanceEnergyUsageIdle() {
        return instanceEnergyUsageIdle;
    }

    public void setInstanceEnergyUsageIdle(final double instanceEnergyUsageIdle) {
        this.instanceEnergyUsageIdle = instanceEnergyUsageIdle;
    }

    public double getInstanceEnergyUsageFull() {
        return instanceEnergyUsageFull;
    }

    public void setInstanceEnergyUsageFull(final double instanceEnergyUsageFull) {
        this.instanceEnergyUsageFull = instanceEnergyUsageFull;
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
