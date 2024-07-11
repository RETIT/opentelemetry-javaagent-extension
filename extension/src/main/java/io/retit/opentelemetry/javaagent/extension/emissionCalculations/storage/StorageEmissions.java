package io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StorageEmissions {

    public static final double STORAGE_EMISSIONS_HDD_PER_TB_HOUR = 0.00065;
    public static final double STORAGE_EMISSIONS_SSD_PER_TB_HOUR = 0.0012;

    private static final Map<String, Double> regionCo2Map = new HashMap<>();

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(StorageEmissions.class.getResourceAsStream("/grid-emissions-factors-aws.csv"))))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String region = parts[0].trim();
                    double co2Value = Double.parseDouble(parts[3].trim());
                    regionCo2Map.put(region, co2Value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CO2 values from CSV file", e);
        }
    }

    public static double calculateStorageEmissions(StorageType storageType, String region, double amountInBytes) {
        //double storageSize = amountInBytes / 1024 / 1024 / 1024 / 1024;
        double storageSize = amountInBytes;
        System.out.println(storageSize);
        if (storageType == StorageType.SSD) {
            System.out.println("SSD storage set");
            storageSize *= STORAGE_EMISSIONS_HDD_PER_TB_HOUR;
        } else {
            System.out.println("HDD or nothing set");
            storageSize *= STORAGE_EMISSIONS_SSD_PER_TB_HOUR;
        }
        storageSize *= getRegionMultiplierFromCSVFile(region);
        System.out.println("total emissions: " + storageSize);
        return storageSize;
    }

    public static double getRegionMultiplierFromCSVFile(String region) {
        System.out.println("Region: " + region);
        System.out.println("Region CO2: " + regionCo2Map.getOrDefault(region, 0.0));
        return regionCo2Map.getOrDefault(region, 0.0);
    }
}