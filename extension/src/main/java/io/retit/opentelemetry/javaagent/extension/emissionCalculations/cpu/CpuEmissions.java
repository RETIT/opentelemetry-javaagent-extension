package io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu;

import io.retit.opentelemetry.javaagent.extension.config.EnvVariables;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CpuEmissions {

    private static CpuEmissions instance;
    private static Double instanceEnergyUsageIdle;
    private static Double instanceEnergyUsageFull;

    private CpuEmissions() {
    }

    public static CpuEmissions getInstance() {
        if (instance == null) {
            instance = new CpuEmissions();
        }
        return instance;
    }

    private void getInstanceDetails(String instanceType) {
        if (instanceEnergyUsageIdle != null && instanceEnergyUsageFull != null) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(CpuEmissions.class.getResourceAsStream("/aws-instances.csv"))))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.get("Instance type").trim().equalsIgnoreCase(instanceType.trim())) {
                    instanceEnergyUsageIdle = Double.parseDouble(csvRecord.get("Instance @ Idle").replace("\"", "").trim().replace(',', '.'));
                    instanceEnergyUsageFull = Double.parseDouble(csvRecord.get("Instance @ 100%").replace("\"", "").trim().replace(',', '.'));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load instance details from CSV file", e);
        }
    }

    public double calculateCpuEmissions(String instanceType, double cpuTimeUsed, double cpuUtilization) {
        if (instanceType == null || instanceType.isEmpty()) {
            throw new RuntimeException("Instance environment variable not provided or found");
        }

        getInstanceDetails(EnvVariables.getEnvInstance().getInstance());

        return instanceEnergyUsageIdle + cpuUtilization * (instanceEnergyUsageFull - instanceEnergyUsageIdle) / 1000 * cpuTimeUsed;
    }

    public double calculateCpuEmissions(String instanceType, double cpuTimeUsed) {
        System.out.println("instanceType for emissions: " + instanceType);
        System.out.println("cpuTimeUsed for emissions: " + cpuTimeUsed);

        if (instanceType == null || instanceType.isEmpty()) {
            throw new RuntimeException("Instance environment variable not provided or found");
        }

        getInstanceDetails(EnvVariables.getEnvInstance().getInstance());

        return instanceEnergyUsageIdle + 0.5 * (instanceEnergyUsageFull - instanceEnergyUsageIdle) / 1000 * cpuTimeUsed;
    }
}
