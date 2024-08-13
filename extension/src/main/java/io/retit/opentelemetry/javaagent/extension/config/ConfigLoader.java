package io.retit.opentelemetry.javaagent.extension.config;

import io.retit.opentelemetry.javaagent.extension.emissions.EmissionCoefficients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * ConfigLoader is responsible for loading configuration settings and instance details
 * from environment variables and CSV files.
 */
public class ConfigLoader {

    private final Logger logger = Logger.getLogger(ConfigLoader.class.getName());

    private static final ConfigLoader CONFIG_INSTANCE = new ConfigLoader();

    private final String serviceName;
    private final String storageType;
    private final String region;
    private final String cloudInstanceName;
    private final String microarchitecture;
    private final String cloudProvider;
    private final Double gridEmissionsFactor;
    private final Double instanceEnergyUsageIdle;
    private final Double instanceEnergyUsageFull;
    private final Double instanceVCpu;
    private final Double platformTotalVcpu;
    private final Double totalEmbodiedEmissions;
    private final Double pueValue;
    private final Integer cpuCount;
    private final Double cpuUtilization;
    private final Double[] cloudInstanceDetails = new Double[4];

    /**
     * Returns the singleton instance of ConfigLoader.
     *
     * @return the singleton instance of ConfigLoader
     */
    public static ConfigLoader getConfigInstance() {
        return CONFIG_INSTANCE;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getRegion() {
        return region;
    }

    public String getCloudInstanceName() {
        return cloudInstanceName;
    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    public Double getGridEmissionsFactor() {
        return gridEmissionsFactor;
    }

    public Double getInstanceEnergyUsageIdle() {
        return instanceEnergyUsageIdle;
    }

    public Double getInstanceEnergyUsageFull() {
        return instanceEnergyUsageFull;
    }

    public Double getInstanceVCpu() {
        return instanceVCpu;
    }

    public Double getPlatformTotalVcpu() {
        return platformTotalVcpu;
    }

    public Double getTotalEmbodiedEmissions() {
        return totalEmbodiedEmissions;
    }

    public Double getPueValue() {
        return pueValue;
    }

    public Integer getCpuCount() {
        return cpuCount;
    }

    public Double getCpuUtilization() {
        return cpuUtilization;
    }

    private ConfigLoader() {
        this.serviceName = initializeServiceName();
        this.storageType = initializeStorageType();
        this.region = initializeRegion();
        this.cloudInstanceName = initializeInstance();
        this.cpuCount = initializeCpuCount();
        this.cpuUtilization = initializeCpuUtilization();
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
        String envServiceName = System.getenv("SERVICE_NAME");
        return envServiceName == null || envServiceName.isEmpty() ? "your-service" : envServiceName;
    }

    private String initializeStorageType() {
        String envStorageType = System.getenv("STORAGE_TYPE");
        return envStorageType == null || envStorageType.isEmpty() ? "HDD" : "SSD";
    }

    private String initializeRegion() {
        String envRegion = System.getenv("REGION");
        return envRegion != null && !envRegion.isEmpty() ? envRegion.toUpperCase() : "not-set";
    }

    private String initializeInstance() {
        String envInstance = System.getenv("INSTANCE");
        if ("SERVERLESS".equalsIgnoreCase(envInstance)) {
            return "SERVERLESS";
        } else if (!envInstance.isEmpty()) {
            return envInstance.toUpperCase();
        } else {
            return "not-set";
        }
    }

    private Integer initializeCpuCount() {
        String envCpuCount = System.getenv("CPU_COUNT");
        if (envCpuCount == null) {
            return 1;
        }
        try {
            return Integer.valueOf(envCpuCount);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Double initializeCpuUtilization() {
        String envCpuUtilization = System.getenv("CPU_UTILIZATION_IN_PERCENT");
        if (envCpuUtilization == null) {
            return 0.5;
        }
        try {
            return Double.valueOf(envCpuUtilization);
        } catch (NumberFormatException e) {
            return 0.5;
        }
    }

    private String initializeMicroarchitecture() {
        String envMicroarchitecture = System.getenv("MICROARCHITECTURE");
        return envMicroarchitecture == null || envMicroarchitecture.trim().isEmpty() ? null : envMicroarchitecture.toUpperCase();
    }

    private String initializeCloudProvider() {
        String providerName = System.getenv("CLOUD_PROVIDER");
        return providerName == null || providerName.isEmpty() ? "not-set" : providerName.toUpperCase();
    }

    /**
     * Initializes the grid emission factor for the specified envRegion.
     * The grid emission factor is determined based on the cloud provider and Region.
     * It reads the emission factors from CSV files specific to each cloud provider.
     * The provided CSV files come from <a href="https://github.com/cloud-carbon-footprint/cloud-carbon-coefficients/tree/main/data">...</a>.
     *
     * @param envRegion the Region for which the grid emission factor is to be initialized
     * @return the grid emission factor in kilograms per kWh
     */
    private Double initializeGridEmissionFactor(String envRegion) {
        double gridEmissionFactorMetricTonPerKwh = 0.0;
        if ("AWS".equalsIgnoreCase(cloudProvider)) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/grid-emissions/grid-emissions-factors-aws.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvRegion = fields[0].trim();
                    if (csvRegion.equalsIgnoreCase(envRegion.trim())) {
                        String co2eValue = fields[3]; //
                        gridEmissionFactorMetricTonPerKwh = Double.parseDouble(co2eValue);
                        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
                    }
                }
            } catch (IOException e) {
                logger.warning("Failed to load grid emission factor from CSV file");
            }
        } else if ("AZURE".equalsIgnoreCase(cloudProvider)) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/grid-emissions/grid-emissions-factors-azure.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvRegion = fields[0].trim();
                    if (csvRegion.equalsIgnoreCase(envRegion.trim())) {
                        gridEmissionFactorMetricTonPerKwh = Double.parseDouble(fields[3].trim());
                        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
                    }
                }
            } catch (IOException e) {
                logger.warning("Failed to load grid emission factor from CSV file");
            }
        } else if ("GCP".equalsIgnoreCase(cloudProvider)) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/grid-emissions/grid-emissions-factors-gcp.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvRegion = fields[0].trim();
                    if (csvRegion.equalsIgnoreCase(envRegion.trim())) {
                        gridEmissionFactorMetricTonPerKwh = Double.parseDouble(fields[2].trim());
                        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
                    }
                }
            } catch (IOException ignored) {
                logger.warning("Failed to load grid emission factor from CSV file");
            }

        }
        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
    }

    /**
     * Initializes the cloud instance details for the specified instance type.
     * The details are determined based on the cloud provider and instance type.
     * It reads the instance details from CSV files specific to each cloud provider.
     * The provided CSV files come from:
     * <a href="https://github.com/cloud-carbon-footprint/cloud-carbon-coefficients/tree/main/data">data</a>,
     * <a href="https://github.com/cloud-carbon-footprint/cloud-carbon-coefficients/tree/main/output">output</a>.
     *
     * @param instanceType the type of the instance for which the details are to be initialized
     */
    private void initializeCloudInstanceDetails(String instanceType) {
        if ("AZURE".equalsIgnoreCase(cloudProvider)) {
            initializeCloudInstanceDetailsForAzure(instanceType);
        } else if ("AWS".equalsIgnoreCase(cloudProvider)) {
            initializeCloudInstanceDetailsForAws(instanceType);
        } else if ("GCP".equalsIgnoreCase(cloudProvider)) {
            initializeCloudInstanceDetailsForGcp(instanceType);
        } else {
            cloudInstanceDetails[0] = 0.0; // Number of Instance vCPU
            cloudInstanceDetails[1] = 0.0; // Number of Platform Total vCPU
            cloudInstanceDetails[2] = 0.0; // Instance Watt usage @ Idle
            cloudInstanceDetails[3] = 0.0; // Instance Watt usage @ 100%
        }
    }

    private void initializeCloudInstanceDetailsForGcp(String instanceType) {
        try (BufferedReader reader = new BufferedReader(new
                InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/instances/gcp-instances.csv"))))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String csvInstanceType = fields[1].trim();
                if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                    cloudInstanceDetails[0] = Double.parseDouble(fields[3].trim()); // Number of Instance vCPU
                    cloudInstanceDetails[1] = Double.parseDouble(fields[5].trim()); // Number of Platform Total vCPU
                    break;
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to load instance details from CSV file");
        }
        if (microarchitecture != null) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/instances/coefficients-gcp-use.csv"))))) {
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    String csvMicroarchitecture = fields[1].trim();
                    if (csvMicroarchitecture.equals(microarchitecture)) {
                        cloudInstanceDetails[2] = Double.parseDouble(fields[2].trim()); // Instance Watt usage @ Idle
                        cloudInstanceDetails[3] = Double.parseDouble(fields[3].trim()); // Instance Watt usage @ 100%
                        return;
                    }

                }
            }
            catch (IOException e) {
                logger.warning("Failed to load instance details from CSV file");
            }
        } else {
            cloudInstanceDetails[2] = EmissionCoefficients.AVERAGE_MIN_WATT_GCP;
            cloudInstanceDetails[3] = EmissionCoefficients.AVERAGE_MAX_WATT_GCP;
        }
    }

    private void initializeCloudInstanceDetailsForAws(String instanceType) {
        try (BufferedReader reader = new BufferedReader(new
                InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/instances/aws-instances.csv"))))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = parseCSVLine(line);
                String csvInstanceType = fields[0].trim();
                if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                    cloudInstanceDetails[0] = Double.parseDouble(fields[2].trim()); // Instance vCPU
                    cloudInstanceDetails[1] = Double.parseDouble(fields[3].trim()); // Platform Total Number of vCPU
                    cloudInstanceDetails[2] = Double.parseDouble(fields[27].replace("\"", "").trim().replace(',', '.')); // Instance Watt usage @ Idle
                    cloudInstanceDetails[3] = Double.parseDouble(fields[30].replace("\"", "").trim().replace(',', '.')); // Instance Watt usage @ 100%
                    return;
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to load instance details from CSV file");
        }
    }

    private void initializeCloudInstanceDetailsForAzure(String instanceType) {
        try (BufferedReader reader = new BufferedReader(new
                InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/instances/azure-instances.csv"))))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = parseCSVLine(line);
                String csvInstanceType = fields[1].trim();
                if (csvInstanceType.equalsIgnoreCase(instanceType.trim())) {
                    cloudInstanceDetails[0] = Double.parseDouble(fields[3].trim()); //Number of Instance vCPU
                    cloudInstanceDetails[1] = Double.parseDouble(fields[5].trim()); //Number of Platform Total vCPU
                    break;
                }
            }
        } catch (IOException ignored) {
            logger.warning("Failed to load instance details from CSV file");

        }
        if (microarchitecture != null) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/instances/coefficients-azure-use.csv"))))) {
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    String csvMicroarchitecture = fields[1].trim();
                    if (csvMicroarchitecture.equals(microarchitecture)) {
                        cloudInstanceDetails[2] = Double.parseDouble(fields[2].trim()); // Instance Watt usage @ Idle
                        cloudInstanceDetails[3] = Double.parseDouble(fields[3].trim()); // Instance Watt usage @ 100%
                        return;
                    }
                }
                cloudInstanceDetails[2] = EmissionCoefficients.AVERAGE_MIN_WATT_AZURE;
                cloudInstanceDetails[3] = EmissionCoefficients.AVERAGE_MAX_WATT_AZURE;
            } catch (IOException e) {
                logger.warning("Failed to load instance details from CSV file");
            }
        } else {
            cloudInstanceDetails[2] = EmissionCoefficients.AVERAGE_MIN_WATT_AZURE;
            cloudInstanceDetails[3] = EmissionCoefficients.AVERAGE_MAX_WATT_AZURE;
        }
    }

    /**
     * Initializes the total embodied emissions for the specified instance type.
     * The total embodied emissions are determined based on the cloud provider and instance type.
     * It reads the embodied emissions from CSV files specific to each cloud provider.
     * The provided CSV files come from:
     * <a href="https://github.com/cloud-carbon-footprint/cloud-carbon-coefficients/tree/main/output">output</a>.
     * The embodied emissions are in kilograms of CO2e.
     *
     * @param instanceType the type of the instance for which the embodied emissions are to be initialized
     * @return the total embodied emissions in kilograms of CO2e
     */
    public Double totalEmbodiedEmissions(String instanceType) {
        if ("AWS".equalsIgnoreCase(cloudProvider)) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/embodied-emissions/coefficients-aws-embodied.csv"))))) {
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
                logger.warning("Failed to load total embodied emissions from CSV file");
            }
        } else if ("GCP".equalsIgnoreCase(cloudProvider)) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/embodied-emissions/coefficients-gcp-embodied-mean.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvInstance = fields[1].trim();
                    if (csvInstance.equalsIgnoreCase(instanceType.trim())) {
                        return Double.parseDouble(fields[2].trim());
                    }
                }
            } catch (IOException e) {
                logger.warning("Failed to load total embodied emissions from CSV file");
            }
        } else if ("AZURE".equalsIgnoreCase(cloudProvider)) {
            try (BufferedReader reader = new BufferedReader(new
                    InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/embodied-emissions/coefficients-azure-embodied.csv"))))) {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    String csvInstance = fields[2].trim();
                    if (csvInstance.equalsIgnoreCase(instanceType.trim())) {
                        return Double.parseDouble(fields[8].trim());
                    }
                }
            } catch (IOException e) {
                logger.warning("Failed to load total embodied emissions from CSV file");
            }
        }
        return 0.0;
    }

    private Double initializePueValue() {
        double returnValue = 0.0;
        if ("AWS".equalsIgnoreCase(cloudProvider)) {
            returnValue = EmissionCoefficients.AWS_PUE;
        } else if ("AZURE".equalsIgnoreCase(cloudProvider)) {
            returnValue = EmissionCoefficients.AZURE_PUE;
        } else if ("GCP".equalsIgnoreCase(cloudProvider)) {
            returnValue = EmissionCoefficients.GCP_PUE;
        }
        return returnValue;
    }

    private String[] parseCSVLine(String line) {
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