package io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

public class CpuEmissions {

    private static CpuEmissions instance;
    private final Double instanceEnergyUsageIdle;
    private final Double instanceEnergyUsageFull;
    private final Double pueValue;

    private CpuEmissions() {
        ConfigLoader configLoader = ConfigLoader.getConfigInstance();
        this.instanceEnergyUsageIdle = configLoader.getInstanceEnergyUsageIdle();
        this.instanceEnergyUsageFull = configLoader.getInstanceEnergyUsageFull();
        this.pueValue = configLoader.getPueValue();
    }

    public static CpuEmissions getInstance() {
        if (instance == null) {
            instance = new CpuEmissions();
        }
        return instance;
    }

    public double calculateCpuEmissions(double cpuTimeUsed, double cpuUtilization) {
        return instanceEnergyUsageIdle + cpuUtilization * (instanceEnergyUsageFull - instanceEnergyUsageIdle) / 1000 * cpuTimeUsed;
    }

    public double calculateCpuEmissions(double cpuTimeUsed) {
        cpuTimeUsed = 10000; //simplification for now
        return (instanceEnergyUsageIdle + 0.5 * (instanceEnergyUsageFull - instanceEnergyUsageIdle)) / 1000 * cpuTimeUsed
                * pueValue;
    }
}
