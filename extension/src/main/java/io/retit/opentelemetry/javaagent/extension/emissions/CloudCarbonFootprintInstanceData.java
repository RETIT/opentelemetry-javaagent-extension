package io.retit.opentelemetry.javaagent.extension.emissions;

public class CloudCarbonFootprintInstanceData {
    // Number of Instance vCPU
    private double instanceVCpuCount;
    // Number of Platform Total vCPU
    private double platformTotalVcpu;
    // Instance Watt usage @ Idle
    private double instanceEnergyUsageIdle;
    // Instance Watt usage @ 100%
    private double instanceEnergyUsageFull;

    public CloudCarbonFootprintInstanceData() {
    }

    public CloudCarbonFootprintInstanceData(final double instanceVCpuCount,
                                            final double platformTotalVcpu,
                                            final double instanceEnergyUsageIdle,
                                            final double instanceEnergyUsageFull) {
        this.instanceVCpuCount = instanceVCpuCount;
        this.platformTotalVcpu = platformTotalVcpu;
        this.instanceEnergyUsageIdle = instanceEnergyUsageIdle;
        this.instanceEnergyUsageFull = instanceEnergyUsageFull;
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
}