package io.retit.opentelemetry.javaagent.extension.config;

import io.retit.opentelemetry.javaagent.extension.emissions.cpu.CpuEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.storage.StorageEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.storage.StorageType;
import lombok.Getter;

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
    private final CloudProvider cloudProvider;
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
    private final Double[] cloudInstanceDetails = new Double[4];

    private ConfigLoader() {
        this.storageType = initializeStorageType();
        this.region = initializeRegion();
        this.cloudInstanceName = initializeInstance();
        this.gridEmissionsFactor = initializeGridEmissionFactor(region);
        initializeCloudInstanceDetails(cloudInstanceName);
        this.instanceEnergyUsageIdle = cloudInstanceDetails[0];
        this.instanceEnergyUsageFull = cloudInstanceDetails[1];
        this.instanceVCpu = cloudInstanceDetails[2];
        this.platformTotalVcpu = cloudInstanceDetails[3];
        this.totalEmbodiedEmissions = totalEmbodiedEmissions(cloudInstanceName);
        this.pueValue = initializePueValue();
        this.cloudProvider = initializeCloudProvider();
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
            throw new IllegalStateException("INSTANCE environment variable is required but not set. Set it to 'SERVERLESS' " +
                    "in case of serverless deployment");
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
                Objects.requireNonNull(getClass().getResourceAsStream("/grid-emissions/grid-emissions-factors-aws.csv"))))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length >= 4) {
                    String csvRegion = fields[0].trim();
                    if (csvRegion.equalsIgnoreCase(region.trim())) {
                        String co2eValue = fields[3].replace("\"", "").trim().replace(',', '.'); //
                        returnValue = Double.parseDouble(co2eValue);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CO2 values from CSV file", e);
        }
        System.out.println("Region: " + region + " has grid emission of: " + returnValue);
        return returnValue;
    }

    private void initializeCloudInstanceDetails(String instanceType) {
        //Double[] cloudInstanceDetails = new Double[4];
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/instances/aws-instances.csv"))))) {
            String line;
            // Read the header line to skip it
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 31) { // Ensure there are enough fields
                    String csvInstanceType = fields[0].trim();
                    if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                        cloudInstanceDetails[0] = Double.parseDouble(fields[27].replace("\"", "").trim().replace(',', '.')); // Instance @ Idle
                        cloudInstanceDetails[1] = Double.parseDouble(fields[30].replace("\"", "").trim().replace(',', '.')); // Instance @ 100%
                        cloudInstanceDetails[2] = Double.parseDouble(fields[2].trim()); // Instance vCPU
                        cloudInstanceDetails[3] = Double.parseDouble(fields[3].trim()); // Platform Total Number of vCPU
                        System.out.println("instance " + instanceType + " instanceEnergyUsageIdle: " + cloudInstanceDetails[0]
                                + " instanceEnergyUsageFull: " + cloudInstanceDetails[1]
                                + " instanceVCpu: " + cloudInstanceDetails[2]
                                + " platformTotalVcpu: " + cloudInstanceDetails[3]);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load instance details from CSV file", e);
        }
    }

    public Double totalEmbodiedEmissions (String instanceType) {
        Double totalValue = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/embodied-emissions/coefficients-aws-embodied.csv"))))) {
            String line;
            // Read the header line to skip it
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 6) { // Ensure there are enough fields
                    String csvInstanceType = fields[1].trim(); // Assuming 'type' is the second field
                    if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                        totalValue = Double.parseDouble(fields[6].trim()); // Assuming 'total' is the seventh field
                        System.out.println("Instance: " + instanceType + " Total: " + totalValue);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load total value from CSV file", e);
        }
        return totalValue;
    }

    private Double initializePueValue() {
        Double returnValue = null;
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

        if (cloudProvider == CloudProvider.AWS) {
            returnValue = 1.135;
        } else if (cloudProvider == CloudProvider.AZURE) {
            returnValue = 1.125;
        } else if (cloudProvider == CloudProvider.GCP) {
            returnValue = 1.1;
        }

        System.out.println("PUE value: " + returnValue);
        return returnValue;
    }

    private String[] parseCSVLine(String line) {
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        java.util.List<String> fields = new java.util.ArrayList<>();
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes; // Toggle the state of inQuotes
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder(); // Start a new field
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString()); // Add the last field
        return fields.toArray(new String[0]);
    }
}