package io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

import static io.retit.opentelemetry.javaagent.extension.config.CloudProvider.AWS;

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
