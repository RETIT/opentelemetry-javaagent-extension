package io.retit.opentelemetry.javaagent.extension.config;

import io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu.CpuEmissions;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage.StorageEmissions;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage.StorageType;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ConfigLoader {

    @Getter
    private static final ConfigLoader configInstance = new ConfigLoader();

    @Getter
    private final StorageType storageType;
    @Getter
    private final String region;
    @Getter
    private final String cloudInstanceName;
    @Getter
    private final Double gridEmissionsFactor;
    @Getter
    private final Double instanceEnergyUsageIdle;
    @Getter
    private final Double instanceEnergyUsageFull;
    @Getter
    private final Double instanceVCpu;
    @Getter
    private final Double platformTotalVcpu;
    @Getter
    private final Double totalEmbodiedEmissions;
    @Getter
    private final Double pueValue;
    private final Double[] cloudInstanceDetails = new Double[5];

    private ConfigLoader() {
        this.storageType = initializeStorageType();
        this.region = initializeRegion();
        this.cloudInstanceName = initializeInstance();
        this.gridEmissionsFactor = initializeGridEmissionFactor(region);
        this.instanceEnergyUsageIdle = initializeCloudInstanceDetails(cloudInstanceName)[0];
        this.instanceEnergyUsageFull = cloudInstanceDetails[1];
        this.instanceVCpu = cloudInstanceDetails[2];
        this.platformTotalVcpu = cloudInstanceDetails[3];
        this.totalEmbodiedEmissions = cloudInstanceDetails[4];
        this.pueValue = initializePueValue();
    }

    private StorageType initializeStorageType() {
        String storageType = System.getenv("STORAGE_TYPE").trim();
        System.out.println("Storage type: " + storageType);
        if (storageType.isEmpty()) {
            throw new IllegalStateException("STORAGE_TYPE environment variable is not set");
        }
        if (storageType.equalsIgnoreCase("HDD")) {
            return StorageType.HDD;
        } else {
            return StorageType.SSD;
        }
    }

    private String initializeRegion() {
        String region = System.getenv("REGION").trim();
        System.out.println("Region: " + region);
        if (region.isEmpty()) {
            throw new IllegalStateException("REGION environment variable is required but not set");
        }
        return region;
    }

    private String initializeInstance() {
        String instance = System.getenv("INSTANCE").trim();
        System.out.println("Instance: " + instance);
        if (instance.isEmpty()) {
            throw new IllegalStateException("INSTANCE environment variable is required but not set");
        }
        return instance;
    }

    private CloudProvider initializeCloudProvider() {
        String providerName = System.getenv("CLOUD_PROVIDER");
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalStateException("CLOUD_PROVIDER environment variable is required but not set");
        }

        CloudProvider cloudProvider;
        try {
            cloudProvider = CloudProvider.valueOf(providerName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid CLOUD_PROVIDER value: " + providerName);
        }
        return cloudProvider;
    }

    private Double initializeGridEmissionFactor(String region) {
        Double returnValue = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(StorageEmissions.class.getResourceAsStream("/grid-emissions-factors-aws.csv"))))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.get("Region").trim().equalsIgnoreCase(region.trim())) {
                    returnValue = Double.parseDouble(csvRecord.get("CO2e (metric ton/kWh)").replace("\"", "").trim().replace(',', '.'));
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CO2 values from CSV file", e);
        }
        System.out.println("Region: " + region + " has grid emission of: " + returnValue);
        return returnValue;
    }

    private Double[] initializeCloudInstanceDetails(String instanceType) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(CpuEmissions.class.getResourceAsStream("/instances/aws-details.csv"))))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.get("Instance Type").trim().equalsIgnoreCase(instanceType.trim())) {
                    cloudInstanceDetails[0] = Double.parseDouble(csvRecord.get("Instance @ Idle").trim());
                    cloudInstanceDetails[1] = Double.parseDouble(csvRecord.get("Instance @ 100%").trim());
                    cloudInstanceDetails[2] = Double.parseDouble(csvRecord.get("Instance vCPU").trim());
                    cloudInstanceDetails[3] = Double.parseDouble(csvRecord.get("Platform Total Number of vCPU").trim());
                    cloudInstanceDetails[4] = Double.parseDouble(csvRecord.get("Total Embodied Emissions").trim());
                    System.out.println("instance " + instanceType + "instanceEnergyUsageIdle: " + cloudInstanceDetails[0] + " instanceEnergyUsageFull: "
                            + cloudInstanceDetails[1] + " instanceVCpu: " + cloudInstanceDetails[2] + " platformTotalVcpu: "
                            + cloudInstanceDetails[3] + " totalEmbodiedEmissions: " + cloudInstanceDetails[4]);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load instance details from CSV file", e);
        }
        return cloudInstanceDetails;
    }

    private Double initializePueValue() {
        Double returnValue;
        String providerName = System.getenv("CLOUD_PROVIDER");
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalStateException("CLOUD_PROVIDER environment variable is required but not set");
        }

        CloudProvider cloudProvider;
        try {
            cloudProvider = CloudProvider.valueOf(providerName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid CLOUD_PROVIDER value: " + providerName);
        }

        returnValue = switch (cloudProvider) {
            case AWS -> 1.135;
            case AZURE -> 1.125;
            case GCP -> 1.1;
        };
        System.out.println("PUE value: " + returnValue);
        return returnValue;
    }
}