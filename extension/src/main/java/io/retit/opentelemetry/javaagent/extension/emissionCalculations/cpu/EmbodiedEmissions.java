package io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

public class EmbodiedEmissions {

    private static EmbodiedEmissions instance;
    ConfigLoader configLoader;

    private EmbodiedEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
    }

    public static EmbodiedEmissions getInstance() {
        if (instance == null) {
            instance = new EmbodiedEmissions();
        }
        return instance;
    }

    public double calculateEmbodiedEmissionsInGramm(double cpuTimeUsedInHours) {
        cpuTimeUsedInHours = 1; //simplification for now
        return configLoader.getTotalEmbodiedEmissions() * 0.0289 * configLoader.getInstanceVCpu() / configLoader.getPlatformTotalVcpu()
                * cpuTimeUsedInHours / 1000;
    }
}
