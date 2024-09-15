package io.retit.opentelemetry.javaagent.extension.emissions;

import io.opentelemetry.api.internal.StringUtils;
import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * ConfigLoader is responsible for loading configuration settings and instance details
 * from environment variables and CSV files.
 */
public class CloudCarbonFootprintData {

    private static final Logger logger = Logger.getLogger(CloudCarbonFootprintData.class.getName());
    private static final CloudCarbonFootprintData CONFIG_INSTANCE = new CloudCarbonFootprintData();
    private static final double DOUBLE_ZERO = 0.0;
    private static final char QUOTE_CHAR = '\"';

    private final String microarchitecture;
    private final Double gridEmissionsFactor;
    private final Double totalEmbodiedEmissions;
    private final Double pueValue;
    private final Integer cpuCount;
    private final Double cpuUtilization;
    private final CloudCarbonFootprintInstanceData cloudInstanceDetails;

    private CloudCarbonFootprintData() {
        this.cpuCount = initializeCpuCount();
        this.cpuUtilization = initializeCpuUtilization();
        this.microarchitecture = initializeMicroarchitecture();
        this.gridEmissionsFactor = initializeGridEmissionFactor(InstanceConfiguration.getCloudProviderRegion());
        cloudInstanceDetails = initializeCloudInstanceDetails(InstanceConfiguration.getCloudProviderInstanceType());
        this.totalEmbodiedEmissions = getTotalEmbodiedEmissionsForInstanceType(InstanceConfiguration.getCloudProviderInstanceType());
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

    public CloudCarbonFootprintInstanceData getCloudInstanceDetails() {
        return cloudInstanceDetails;
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
        return envMicroarchitecture == null || StringUtils.isNullOrEmpty(envMicroarchitecture) ? null : envMicroarchitecture.toUpperCase(Locale.ENGLISH);
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

        List<String[]> csvLines = readAllCSVLinesExceptHeader(csvFile);
        for (String[] lineFields : csvLines) {
            String csvInstance = lineFields[instanceTypeOrRegionCsvField].trim();
            if (csvInstance.equalsIgnoreCase(instanceTypeOrRegion.trim())) {
                return Double.parseDouble(lineFields[csvField].trim());
            }
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
     * @param vmInstanceType the type of the instance for which the details are to be initialized
     */
    private CloudCarbonFootprintInstanceData initializeCloudInstanceDetails(final String vmInstanceType) {
        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = null;

        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            cloudVMInstanceDetails = initializeCloudInstanceDetailsForAzure(vmInstanceType);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            cloudVMInstanceDetails = initializeCloudInstanceDetailsForAws(vmInstanceType);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            cloudVMInstanceDetails = initializeCloudInstanceDetailsForGcp(vmInstanceType);
        } else {
            cloudVMInstanceDetails = new CloudCarbonFootprintInstanceData(0.0, 0.0, 0.0, 0.0);
        }

        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for GCP instances
     *
     * @param vmInstanceType - the VM instance type
     */
    private CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsForGcp(final String vmInstanceType) {

        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = initializeCloudInstanceDetailsCommon("/instances/gcp-instances.csv", "/instances/coefficients-gcp-use.csv", vmInstanceType);

        if (cloudVMInstanceDetails.getInstanceEnergyUsageIdle() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setInstanceEnergyUsageIdle(CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_GCP);
        }

        if (cloudVMInstanceDetails.getInstanceEnergyUsageFull() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setInstanceEnergyUsageFull(CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_GCP);
        }

        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for Azure instances.
     *
     * @param vmInstanceType - the VM instance type
     * @return CloudVMInstanceDetails
     */
    private CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsForAzure(final String vmInstanceType) {

        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = initializeCloudInstanceDetailsCommon("/instances/azure-instances.csv", "/instances/coefficients-azure-use.csv", vmInstanceType);

        if (cloudVMInstanceDetails.getInstanceEnergyUsageIdle() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setInstanceEnergyUsageIdle(CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AZURE);
        }

        if (cloudVMInstanceDetails.getInstanceEnergyUsageFull() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setInstanceEnergyUsageFull(CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AZURE);
        }

        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for Azure and GCP instances
     * as AWS has a different file format.
     *
     * @param instanceFileName
     * @param coefficientsFileName
     * @param vmInstanceType
     * @return CloudVMInstanceDetails
     */
    private CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsCommon(final String instanceFileName, final String coefficientsFileName, final String vmInstanceType) {
        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = new CloudCarbonFootprintInstanceData();
        List<String[]> csvLines = readAllCSVLinesExceptHeader(instanceFileName);
        for (String[] lineFields : csvLines) {
            String csvInstanceType = lineFields[1].trim();
            if (csvInstanceType.equalsIgnoreCase(vmInstanceType.trim())) {
                cloudVMInstanceDetails.setInstanceVCpuCount(Double.parseDouble(lineFields[3].trim())); // Number of Instance vCPU
                cloudVMInstanceDetails.setPlatformTotalVcpu(Double.parseDouble(lineFields[5].trim())); // Number of Platform Total vCPU
                break;
            }
        }

        if (microarchitecture != null) {
            csvLines = readAllCSVLinesExceptHeader(coefficientsFileName);
            for (String[] lineFields : csvLines) {
                String csvMicroarchitecture = lineFields[1].trim();
                if (csvMicroarchitecture.equals(microarchitecture)) {
                    cloudVMInstanceDetails.setInstanceEnergyUsageIdle(Double.parseDouble(lineFields[2].trim())); // Instance Watt usage @ Idle
                    cloudVMInstanceDetails.setInstanceEnergyUsageFull(Double.parseDouble(lineFields[3].trim())); // Instance Watt usage @ 100%
                }
            }
        }
        return cloudInstanceDetails;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for AWS  instances.
     * <p>
     * As power consumption is included in the aws instance file a seperate method is used compared to AWS and GCP.
     *
     * @param vmInstanceType - the VM instance type
     */
    private CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsForAws(final String vmInstanceType) {

        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = new CloudCarbonFootprintInstanceData();

        List<String[]> csvLines = readAllCSVLinesExceptHeader("/instances/aws-instances.csv");
        for (String[] lineFields : csvLines) {
            String csvInstanceType = lineFields[0];
            if (csvInstanceType.equalsIgnoreCase(vmInstanceType.trim())) {
                cloudVMInstanceDetails.setInstanceVCpuCount(Double.parseDouble(lineFields[2])); // Instance vCPU
                cloudVMInstanceDetails.setPlatformTotalVcpu(Double.parseDouble(lineFields[3])); // Platform Total Number of vCPU
                cloudVMInstanceDetails.setInstanceEnergyUsageIdle(Double.parseDouble(lineFields[27].replace("\"", "").trim().replace(',', '.'))); // Instance Watt usage @ Idle
                cloudVMInstanceDetails.setInstanceEnergyUsageFull(Double.parseDouble(lineFields[30].replace("\"", "").trim().replace(',', '.'))); // Instance Watt usage @ 100%
            }
        }

        return cloudVMInstanceDetails;
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
    public final Double getTotalEmbodiedEmissionsForInstanceType(final String instanceType) {
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

    /**
     * Utility method for reading a CSV file.
     *
     * @param fileName - the CSV file to read
     * @return a list of String[] representing the lines of the CSV file.
     */
    private List<String[]> readAllCSVLinesExceptHeader(final String fileName) {
        List<String[]> csvLinesWithoutHeader = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new
                InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(fileName))))) {

            String line = reader.readLine();
            boolean firstLine = true;
            while (line != null) {
                if (!firstLine) {
                    String[] fields = parseCSVLine(line);
                    csvLinesWithoutHeader.add(fields);
                }
                line = reader.readLine();
                if (firstLine) {
                    firstLine = false;
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to load instance details from CSV file");
        }

        return csvLinesWithoutHeader;
    }

    /**
     * Parses a single line of a CSV file and ignores fields in quotes.
     *
     * @param line - the line to parse
     * @return - the CSV attributes of the line.
     */
    private String[] parseCSVLine(final String line) {
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        List<String> fields = new ArrayList<>();
        for (char c : line.toCharArray()) {
            if (c == QUOTE_CHAR) {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString().trim());
        return fields.toArray(new String[0]);
    }
}