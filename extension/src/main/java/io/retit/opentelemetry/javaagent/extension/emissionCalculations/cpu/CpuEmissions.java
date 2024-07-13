package io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

public class CpuEmissions {

    private static CpuEmissions instance;
    private ConfigLoader configLoader;

    private CpuEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
    }

    public static CpuEmissions getInstance() {
        if (instance == null) {
            instance = new CpuEmissions();
        }
        return instance;
    }

    public double calculateCpuEmissionsInGramm(double cpuTimeUsed, double cpuUtilization) {
        return configLoader.getInstanceEnergyUsageIdle() + cpuUtilization * (configLoader.getInstanceEnergyUsageFull()
                - configLoader.getInstanceEnergyUsageIdle()) / 1000 * cpuTimeUsed;
    }

    public double calculateCpuEmissionsInGramm(double cpuTimeUsedinHours) {
        cpuTimeUsedinHours = 10000; //simplification for now
        return  configLoader.getInstanceEnergyUsageIdle() * (configLoader.getInstanceEnergyUsageFull()
                - configLoader.getInstanceEnergyUsageIdle()) / 1000 * cpuTimeUsedinHours * configLoader.getPueValue()
                * configLoader.getGridEmissionsFactor() / 1000;
    }
}
