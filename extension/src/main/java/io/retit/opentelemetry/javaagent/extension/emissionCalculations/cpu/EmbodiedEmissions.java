package io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class EmbodiedEmissions {

    private static EmbodiedEmissions instance;
    private static Integer instanceVCpu;
    private static Integer platformTotalVcpu;
    private static Double totalEmbodiedEmissions;

    private EmbodiedEmissions() {
    }

    public static EmbodiedEmissions getInstance() {
        if (instance == null) {
            instance = new EmbodiedEmissions();
        }
        return instance;
    }

    private void loadInstanceDetails(String instanceType) {
        if (instanceVCpu != null && platformTotalVcpu != null && totalEmbodiedEmissions != null) {
            return;
        }

        try (BufferedReader instanceReader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(CpuEmissions.class.getResourceAsStream("/aws-instances.csv"))))) {
            CSVParser instanceParser = new CSVParser(instanceReader, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord record : instanceParser) {
                if (record.get("Instance type").trim().equals(instanceType)) {
                    instanceVCpu = Integer.parseInt(record.get("Instance vCPU").trim());
                    System.out.println("instanceVCpu: " + instanceVCpu);
                    platformTotalVcpu = Integer.parseInt(record.get("Platform Total Number of vCPU").trim());
                    System.out.println("platformTotalVcpu: " + platformTotalVcpu);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load instance details from CSV file", e);
        }

        try (BufferedReader emissionsReader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(CpuEmissions.class.getResourceAsStream("/coefficients-aws-embodied.csv"))))) {
            CSVParser emissionsParser = new CSVParser(emissionsReader, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord record : emissionsParser) {
                if (record.get("type").trim().equals(instanceType)) {
                    totalEmbodiedEmissions = Double.parseDouble(record.get("total").trim());
                    System.out.println("totalEmbodiedEmissions: " + totalEmbodiedEmissions);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load total embodied emissions from CSV file", e);
        }

        if (instanceVCpu == null || platformTotalVcpu == null) {
            throw new RuntimeException("Instance type not found in CSV file: " + instanceType);
        }
    }

    public double calculateEmbodiedEmissionsInGramm(String instanceType, double cpuTimeUsedInHours) {
        loadInstanceDetails(instanceType);

        if (totalEmbodiedEmissions == null) {
            throw new RuntimeException("Total embodied emissions not found for instance type: " + instanceType);
        }
        System.out.println("totalEmbodiedEmissions: " + totalEmbodiedEmissions * 0.0289 * instanceVCpu / platformTotalVcpu * cpuTimeUsedInHours);
        return totalEmbodiedEmissions * 0.0289 * instanceVCpu / platformTotalVcpu * cpuTimeUsedInHours;
    }
}
