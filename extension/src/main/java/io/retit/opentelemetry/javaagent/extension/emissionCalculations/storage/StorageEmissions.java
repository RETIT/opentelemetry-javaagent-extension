package io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StorageEmissions {

    public static StorageEmissions instance;
    public static final double STORAGE_EMISSIONS_HDD_PER_TB_HOUR = 0.00065;
    public static final double STORAGE_EMISSIONS_SSD_PER_TB_HOUR = 0.0012;
    public static Double gridEmissionsFactor;

    private StorageEmissions() {
    }

    public static StorageEmissions getInstance() {
        if (instance == null) {
            instance = new StorageEmissions();
        }
        return instance;
    }

    private void loadRegionCo2ValuesFromCSVFile(String region) {
        if (gridEmissionsFactor != null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(StorageEmissions.class.getResourceAsStream("/grid-emissions-factors-aws.csv"))))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.get("Region").trim().equalsIgnoreCase(region.trim())) {
                    gridEmissionsFactor = Double.parseDouble(csvRecord.get("CO2e (metric ton/kWh)").replace("\"", "").trim().replace(',', '.'));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CO2 values from CSV file", e);
        }
        System.out.println("Region: " + region + " CO2: " + gridEmissionsFactor);
    }

    public double calculateStorageEmissions(StorageType storageType, String region, double amountInBytes) {
        loadRegionCo2ValuesFromCSVFile(region);
        //double storageSize = amountInBytes / 1024 / 1024 / 1024 / 1024;
        double storageSize = amountInBytes; //simplification to get easier numbers
        System.out.println(storageSize);
        if (storageType == StorageType.SSD) {
            System.out.println("SSD storage set");
            storageSize *= STORAGE_EMISSIONS_HDD_PER_TB_HOUR;
        } else {
            System.out.println("HDD or nothing set");
            storageSize *= STORAGE_EMISSIONS_SSD_PER_TB_HOUR;
        }
        storageSize *= gridEmissionsFactor;
        System.out.println("total emissions: " + storageSize);
        return storageSize;
    }
}