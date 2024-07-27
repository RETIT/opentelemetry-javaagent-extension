package io.retit.opentelemetry.javaagent.extension.config;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ConfigLoader {

    @Getter
    private static final ConfigLoader configInstance = new ConfigLoader();

    @Getter
    private final String serviceName;
    @Getter
    private final String storageType;
    @Getter
    private final String region;
    @Getter
    private final String cloudInstanceName;
    @Getter
    private final String microarchitecture;
    @Getter
    private final String cloudProvider;
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
        this.serviceName = initializeServiceName();
        this.storageType = initializeStorageType();
        this.region = initializeRegion();
        this.cloudInstanceName = initializeInstance();
        this.microarchitecture = initializeMicroarchitecture();
        this.cloudProvider = initializeCloudProvider();
        this.gridEmissionsFactor = initializeGridEmissionFactor(region);
        initializeCloudInstanceDetails(cloudInstanceName);
        this.instanceVCpu = cloudInstanceDetails[0];
        this.platformTotalVcpu = cloudInstanceDetails[1];
        this.instanceEnergyUsageIdle = cloudInstanceDetails[2];
        this.instanceEnergyUsageFull = cloudInstanceDetails[3];
        this.totalEmbodiedEmissions = totalEmbodiedEmissions(cloudInstanceName);
        this.pueValue = initializePueValue();
    }

    private String initializeServiceName() {
        String serviceName = System.getenv("SERVICE_NAME");
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalStateException("SERVICE_NAME environment variable is required but not set");
        }
        return serviceName;
    }

    private String initializeStorageType() {
        String storageType = System.getenv("STORAGE_TYPE");
        if (storageType.isEmpty()) {
            throw new IllegalStateException("STORAGE_TYPE environment variable is not set");
        }
        return storageType.equalsIgnoreCase("HDD") ? "HDD" : "SSD";
    }

    private String initializeRegion() {
        String region = System.getenv("REGION");
        if (region == null || region.isEmpty()) {
            throw new IllegalStateException("REGION environment variable is required but not set");
        }
        return region.toUpperCase();
    }

    private String initializeInstance() {
        String instance = System.getenv("INSTANCE");
        if (instance == null || instance.isEmpty()) {
            throw new IllegalStateException("INSTANCE environment variable is required but not set. Set it to 'SERVERLESS' " +
                    "in case of serverless usage.");
        }
        return instance.toUpperCase();
    }

    private String initializeMicroarchitecture() {
        String microarchitecture = System.getenv("MICROARCHITECTURE");
        if (microarchitecture == null || microarchitecture.trim().isEmpty()) {
            return null;
        }
        return microarchitecture.toUpperCase();
    }

    private String initializeCloudProvider() {
        String providerName = System.getenv("CLOUD_PROVIDER");
        if (providerName == null || providerName.isEmpty() || (!providerName.equalsIgnoreCase("AWS")
                && !providerName.equalsIgnoreCase("AZURE") && !providerName.equalsIgnoreCase("GCP"))) {
            throw new IllegalStateException("CLOUD_PROVIDER environment variable not set or not supported (AWS, AZURE, GCP)");
        }
        return providerName.toUpperCase();
    }

    private Double initializeGridEmissionFactor(String region) {
        double gridEmissionFactorMetricTonPerKwh = 0.0;
        if (cloudProvider.equalsIgnoreCase("AWS")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/grid-emissions/grid-emissions-factors-aws.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvRegion = fields[0].trim();
                    if (csvRegion.equalsIgnoreCase(region.trim())) {
                        String co2eValue = fields[3]; //
                        gridEmissionFactorMetricTonPerKwh = Double.parseDouble(co2eValue);
                        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
                    }
                }
                throw new RuntimeException("Region not found in the CSV file");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load CO2 values from CSV file", e);
            }
        } else if (cloudProvider.equalsIgnoreCase("AZURE")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/grid-emissions/grid-emissions-factors-azure.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvRegion = fields[0].trim();
                    if (csvRegion.equalsIgnoreCase(region.trim())) {
                        gridEmissionFactorMetricTonPerKwh = Double.parseDouble(fields[3].trim());
                        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
                    }
                }
                throw new RuntimeException("Region not found in the CSV file");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (cloudProvider.equalsIgnoreCase("GCP")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/grid-emissions/grid-emissions-factors-gcp.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvRegion = fields[0].trim();
                    if (csvRegion.equalsIgnoreCase(region.trim())) {
                        gridEmissionFactorMetricTonPerKwh = Double.parseDouble(fields[2].trim());
                        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
                    }
                }
                throw new RuntimeException("Region not found in the CSV file");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
    }

    private void initializeCloudInstanceDetails(String instanceType) {
        if (cloudProvider.equalsIgnoreCase("AZURE")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/instances/azure-instances.csv"))))) {
                String line;
                reader.readLine();
                boolean found = false;
                while ((line = reader.readLine()) != null) {
                    String[] fields = parseCSVLine(line);
                    String csvInstanceType = fields[1].trim();
                    if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                        cloudInstanceDetails[0] = Double.parseDouble(fields[3].trim());
                        cloudInstanceDetails[1] = Double.parseDouble(fields[5].trim());
                        cloudInstanceDetails[2] = Double.parseDouble(fields[12].trim());
                        cloudInstanceDetails[3] = Double.parseDouble(fields[13].trim());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Instance type not found in CSV file: " + instanceType);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load vCPU values from CSV file", e);
            }
        } else if (cloudProvider.equalsIgnoreCase("AWS")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/instances/aws-instances.csv"))))) {
                String line;
                reader.readLine();
                boolean found = false;
                while ((line = reader.readLine()) != null) {
                    String[] fields = parseCSVLine(line);
                    String csvInstanceType = fields[0].trim();
                    if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                        cloudInstanceDetails[0] = Double.parseDouble(fields[2].trim()); // Instance vCPU
                        cloudInstanceDetails[1] = Double.parseDouble(fields[3].trim()); // Platform Total Number of vCPU
                        cloudInstanceDetails[2] = Double.parseDouble(fields[27].replace("\"", "").trim().replace(',', '.')); // Instance @ Idle
                        cloudInstanceDetails[3] = Double.parseDouble(fields[30].replace("\"", "").trim().replace(',', '.')); // Instance @ 100%
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new IllegalArgumentException("Instance type not found in CSV file");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load instance details from CSV file", e);
            }
        } else if (cloudProvider.equalsIgnoreCase("GCP")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/instances/gcp-instances.csv"))))) {
                reader.readLine();
                String line;
                boolean found = false;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    String csvInstanceType = fields[1].trim();
                    String csvMicroarchitecture = fields[2].trim();
                    if ((microarchitecture == null && csvInstanceType.equalsIgnoreCase(instanceType.trim())) ||
                            (csvInstanceType.equalsIgnoreCase(instanceType.trim()) &&
                                    csvMicroarchitecture.equalsIgnoreCase(microarchitecture))) {
                        cloudInstanceDetails[0] = Double.parseDouble(fields[3].trim());
                        cloudInstanceDetails[1] = Double.parseDouble(fields[5].trim());
                        cloudInstanceDetails[2] = (microarchitecture == null) ? 1 : Double.parseDouble(fields[12].trim());
                        cloudInstanceDetails[3] = (microarchitecture == null) ? 1 : Double.parseDouble(fields[13].trim());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Instance type not found in CSV file: " + instanceType);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load vCPU values from CSV file", e);
            }
        }
    }

    public Double totalEmbodiedEmissions(String instanceType) {
        if (cloudProvider.equalsIgnoreCase("AWS")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/embodied-emissions/coefficients-aws-embodied.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = parseCSVLine(line);
                    if (fields.length >= 6) {
                        String csvInstanceType = fields[1].trim();
                        if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                            return Double.parseDouble(fields[6].trim());
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load total value from CSV file", e);
            }
        } else if (cloudProvider.equalsIgnoreCase("GCP")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/embodied-emissions/coefficients-gcp-embodied-mean.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvInstance = fields[1].trim();
                    if (csvInstance.equalsIgnoreCase(instanceType.trim())) {
                        return Double.parseDouble(fields[2].trim());
                    }
                }
                throw new RuntimeException("Region not found in the CSV file");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load CO2 values from CSV file", e);
            }
        } else if (cloudProvider.equalsIgnoreCase("AZURE")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream("/embodied-emissions/coefficients-azure-embodied.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvInstance = fields[2].trim();
                    if (csvInstance.equalsIgnoreCase(instanceType.trim())) {
                        return Double.parseDouble(fields[8].trim());
                    }
                }
                throw new RuntimeException("Region not found in the CSV file");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load CO2 values from CSV file", e);
            }
        }
        return 0.0;
    }

        private Double initializePueValue () {
            Double returnValue = null;

            if (cloudProvider.equalsIgnoreCase("AWS")) {
                returnValue = 1.135;
            } else if (cloudProvider.equalsIgnoreCase("AZURE")) {
                returnValue = 1.125;
            } else if (cloudProvider.equalsIgnoreCase("GCP")) {
                returnValue = 1.1;
            }
            return returnValue;
        }

        private String[] parseCSVLine (String line) {
            boolean inQuotes = false;
            StringBuilder field = new StringBuilder();
            java.util.List<String> fields = new java.util.ArrayList<>();
            for (char c : line.toCharArray()) {
                if (c == '\"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    fields.add(field.toString());
                    field = new StringBuilder();
                } else {
                    field.append(c);
                }
            }
            fields.add(field.toString());
            return fields.toArray(new String[0]);
        }
    }