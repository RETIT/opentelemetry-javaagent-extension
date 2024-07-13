package io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage;

import io.retit.opentelemetry.javaagent.extension.config.ConfigLoader;

public class StorageEmissions {

    public static StorageEmissions instance;
    public static final double STORAGE_EMISSIONS_HDD_PER_TB_HOUR = 0.00065;
    public static final double STORAGE_EMISSIONS_SSD_PER_TB_HOUR = 0.0012;
    ConfigLoader configLoader;

    private StorageEmissions() {
        configLoader = ConfigLoader.getConfigInstance();
    }

    public static StorageEmissions getInstance() {
        if (instance == null) {
            instance = new StorageEmissions();
        }
        return instance;
    }

    public double calculateStorageEmissions(double amountInBytes) {
        //double storageSize = amountInBytes / 1024 / 1024 / 1024 / 1024;
        double storageSize = 10000; //simplification for now
        System.out.println(storageSize);
        if (configLoader.getStorageType() == StorageType.HDD) {
            storageSize *= STORAGE_EMISSIONS_HDD_PER_TB_HOUR;
        } else {
            storageSize *= STORAGE_EMISSIONS_SSD_PER_TB_HOUR;
        }
        storageSize *= configLoader.getGridEmissionsFactor() * configLoader.getPueValue();
        return storageSize;
    }
}