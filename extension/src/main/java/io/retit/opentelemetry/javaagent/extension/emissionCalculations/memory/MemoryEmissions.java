package io.retit.opentelemetry.javaagent.extension.emissionCalculations.memory;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

public class MemoryEmissions {
    public static MemoryEmissions instance;
    ConfigLoader configLoader;

    private MemoryEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
    }

    public static MemoryEmissions getInstance() {
        if (instance == null) {
            instance = new MemoryEmissions();
        }
        return instance;
    }

    public double calculateMemoryEmissions(double amountInBytes) {
       // double storageSize = amountInBytes / 1024 / 1024 / 1024;
        amountInBytes = 10000; //simplification to get easier numbers
        amountInBytes *= 0.000392 * configLoader.getGridEmissionsFactor() * configLoader.getPueValue();//0.000392 is a fix coefficient
        return amountInBytes;
    }
}
