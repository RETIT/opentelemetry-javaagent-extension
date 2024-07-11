package io.retit.opentelemetry.javaagent.extension.config;

import io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage.StorageType;

public class EnvVariables {

    private static final EnvVariables envInstance = new EnvVariables();

    private final StorageType storageType;
    private final String region;
    private final String instance;

    private EnvVariables() {
        this.storageType = initializeStorageType();
        this.region = initializeRegion();
        this.instance = initializeInstance();
    }

    public static EnvVariables getEnvInstance() {
        return envInstance;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public String getRegion() {
        return region;
    }

    public String getInstance() {
        return instance;
    }

    private StorageType initializeStorageType() {
        String storageType = System.getenv("STORAGE_TYPE");
        storageType = storageType != null ? storageType.trim() : null;

        if (storageType == null || storageType.isEmpty()) {
            throw new IllegalStateException("STORAGE_TYPE environment variable is not set");
        }
        if (storageType.equalsIgnoreCase("HDD")) {
            return StorageType.HDD;
        } else {
            return StorageType.SSD;
        }
    }

    private String initializeRegion() {
        String region = System.getenv("REGION");
        region = region != null ? region.trim() : null;
        if (region == null || region.isEmpty()) {
            throw new IllegalStateException("REGION environment variable is required but not set");
        }
        return region;
    }

    private String initializeInstance() {
        String instance = System.getenv("INSTANCE");
        instance = instance != null ? instance.trim() : null;
        if (instance == null || instance.isEmpty()) {
            throw new IllegalStateException("INSTANCE environment variable is required but not set");
        }
        return instance;
    }
}