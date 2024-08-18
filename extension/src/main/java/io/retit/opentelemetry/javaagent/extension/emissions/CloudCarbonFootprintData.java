package io.retit.opentelemetry.javaagent.extension.emissions;

import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * ConfigLoader is responsible for loading configuration settings and instance details
 * from environment variables and CSV files.
 */
public class CloudCarbonFootprintData {

    private final Logger logger = Logger.getLogger(CloudCarbonFootprintData.class.getName());

    private static final CloudCarbonFootprintData CONFIG_INSTANCE = new CloudCarbonFootprintData();

    private final String microarchitecture;
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

    private CloudCarbonFootprintData() {
        this.cpuCount = initializeCpuCount();
        this.cpuUtilization = initializeCpuUtilization();
        this.microarchitecture = initializeMicroarchitecture();
        this.gridEmissionsFactor = initializeGridEmissionFactor(InstanceConfiguration.getCloudProviderRegion());
        initializeCloudInstanceDetails(InstanceConfiguration.getCloudProviderInstanceType());
        this.instanceVCpu = cloudInstanceDetails[0];
        this.platformTotalVcpu = cloudInstanceDetails[1];
        this.instanceEnergyUsageIdle = cloudInstanceDetails[2];
        this.instanceEnergyUsageFull = cloudInstanceDetails[3];
        this.totalEmbodiedEmissions = totalEmbodiedEmissions(InstanceConfiguration.getCloudProviderInstanceType());
        this.pueValue = initializePueValue();
    }

    /**
     * Returns the singleton instance of ConfigLoader.
     *
     * @return the singleton instance of ConfigLoader
     */
    public static CloudCarbonFootprintData getConfigInstance() {
        return CONFIG_INSTANCE;
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

    /**
     * Initializes the grid emission factor for the specified envRegion.
     * The grid emission factor is determined based on the cloud provider and Region.
     * It reads the emission factors from CSV files specific to each cloud provider.
     * The provided CSV files come from <a href="https://github.com/cloud-carbon-footprint/cloud-carbon-coefficients/tree/main/data">...</a>.
     *
     * @param envRegion the Region for which the grid emission factor is to be initialized
     * @return the grid emission factor in kilograms per kWh
     */
    private Double initializeGridEmissionFactor(final String envRegion) {
        double gridEmissionFactorMetricTonPerKwh = 0.0;
        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            gridEmissionFactorMetricTonPerKwh = getDoubleValueFromCSVForRegionOrInstance("/grid-emissions/grid-emissions-factors-aws.csv", 0, envRegion, 3);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            gridEmissionFactorMetricTonPerKwh = getDoubleValueFromCSVForRegionOrInstance("/grid-emissions/grid-emissions-factors-azure.csv", 0, envRegion, 3);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            gridEmissionFactorMetricTonPerKwh = getDoubleValueFromCSVForRegionOrInstance("/grid-emissions/grid-emissions-factors-gcp.csv", 0, envRegion, 2);
        }
        return gridEmissionFactorMetricTonPerKwh * 1000; // Convert to kilogram per kWh
    }

    private double getDoubleValueFromCSVForRegionOrInstance(final String csvFile, final int instanceTypeOrRegionCsvField, final String instanceTypeOrRegion, final int csvField) {
        try (BufferedReader reader = new BufferedReader(new
                InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(csvFile))))) {
            // skip first line
            reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                String[] fields = line.split(",", -1);
                String csvInstance = fields[instanceTypeOrRegionCsvField].trim();
                if (csvInstance.equalsIgnoreCase(instanceTypeOrRegion.trim())) {
                    return Double.parseDouble(fields[csvField].trim());
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            logger.severe("Failed to load total embodied emissions from CSV file: " + csvFile);
        }
        return 0.0;
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
    private void initializeCloudInstanceDetails(final String instanceType) {
        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            initializeCloudInstanceDetailsForAzure(instanceType);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            initializeCloudInstanceDetailsForAws(instanceType);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            initializeCloudInstanceDetailsForGcp(instanceType);
        } else {
            cloudInstanceDetails[0] = 0.0; // Number of Instance vCPU
            cloudInstanceDetails[1] = 0.0; // Number of Platform Total vCPU
            cloudInstanceDetails[2] = 0.0; // Instance Watt usage @ Idle
            cloudInstanceDetails[3] = 0.0; // Instance Watt usage @ 100%
        }
    }

    private void initializeCloudInstanceDetailsForGcp(final String instanceType) {
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
            } catch (IOException e) {
                logger.warning("Failed to load instance details from CSV file");
            }
        } else {
            cloudInstanceDetails[2] = CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_GCP;
            cloudInstanceDetails[3] = CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_GCP;
        }
    }

    private void initializeCloudInstanceDetailsForAws(final String instanceType) {
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

    private void initializeCloudInstanceDetailsForAzure(final String instanceType) {
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
                cloudInstanceDetails[2] = CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AZURE;
                cloudInstanceDetails[3] = CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AZURE;
            } catch (IOException e) {
                logger.warning("Failed to load instance details from CSV file");
            }
        } else {
            cloudInstanceDetails[2] = CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AZURE;
            cloudInstanceDetails[3] = CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AZURE;
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
    public Double totalEmbodiedEmissions(final String instanceType) {
        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return getDoubleValueFromCSVForRegionOrInstance("/embodied-emissions/coefficients-aws-embodied.csv", 1, instanceType, 6);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return getDoubleValueFromCSVForRegionOrInstance("/embodied-emissions/coefficients-gcp-embodied-mean.csv", 1, instanceType, 2);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return getDoubleValueFromCSVForRegionOrInstance("/embodied-emissions/coefficients-azure-embodied.csv", 2, instanceType, 8);
        }
        return 0.0;
    }

    private Double initializePueValue() {
        double returnValue = 0.0;
        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            returnValue = CloudCarbonFootprintCoefficients.AWS_PUE;
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            returnValue = CloudCarbonFootprintCoefficients.AZURE_PUE;
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            returnValue = CloudCarbonFootprintCoefficients.GCP_PUE;
        }
        return returnValue;
    }

    private String[] parseCSVLine(final String line) {
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        List<String> fields = new ArrayList<>();
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