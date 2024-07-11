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

    private static Double[] getInstanceDetails(String instanceType) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(CpuEmissions.class.getResourceAsStream("/aws-instances.csv"))))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.get("Instance type").trim().equals(instanceType)) {
                    double instanceIdle = Double.parseDouble(csvRecord.get("Instance @ Idle").replace("\"", "").trim().replace(',', '.'));
                    double instanceFull = Double.parseDouble(csvRecord.get("Instance @ 100%").replace("\"", "").trim().replace(',', '.'));
                    System.out.println("value at position 'Instance @ Idle': " + instanceIdle);
                    System.out.println("value at position 'Instance @ 100%': " + instanceFull);
                    return new Double[]{instanceIdle, instanceFull};
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load instance details from CSV file", e);
        }
        throw new RuntimeException("Instance type not found in CSV file: " + instanceType);
    }

    public static double calculateCpuEmissions(String instanceType, double cpuTimeUsed, double cpuUtilization) {
        if (instanceType == null || instanceType.isEmpty()) {
            throw new RuntimeException("Instance environment variable not provided or found");
        }

        Double[] instanceDetails = CpuEmissions.getInstanceDetails(EnvVariables.getEnvInstance().getInstance());

        double instanceIdle = instanceDetails[0];
        double instanceFull = instanceDetails[1];

        return instanceIdle + cpuUtilization * (instanceFull - instanceIdle) / 1000 * cpuTimeUsed;
    }

    public static double calculateCpuEmissions(String instanceType, double cpuTimeUsed) {
        System.out.println("instanceType for emissions: " + instanceType);
        System.out.println("cpuTimeUsed for emissions: " + cpuTimeUsed);

        if (instanceType == null || instanceType.isEmpty()) {
            throw new RuntimeException("Instance environment variable not provided or found");
        }

        Double[] instanceDetails = CpuEmissions.getInstanceDetails(EnvVariables.getEnvInstance().getInstance());

        double instanceIdle = instanceDetails[0];
        double instanceFull = instanceDetails[1];
        System.out.println("instanceIdle: " + instanceIdle);
        System.out.println("instanceFull: " + instanceFull);

        return instanceIdle + 0.5 * (instanceFull - instanceIdle) / 1000 * cpuTimeUsed;
    }
}
