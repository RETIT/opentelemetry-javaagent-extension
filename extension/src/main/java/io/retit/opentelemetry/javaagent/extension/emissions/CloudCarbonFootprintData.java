package io.retit.opentelemetry.javaagent.extension.emissions;

import io.opentelemetry.api.internal.StringUtils;
import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.commons.CSVParser;

import java.util.List;
import java.util.Locale;

/**
 * ConfigLoader is responsible for loading configuration settings and instance details
 * from environment variables and CSV files.
 */
public class CloudCarbonFootprintData {

    private static final CloudCarbonFootprintData CONFIG_INSTANCE = new CloudCarbonFootprintData();
    private static final double DOUBLE_ZERO = 0.0;

    private final String microarchitecture;
    private final Double gridEmissionsFactor;
    private final Double totalEmbodiedEmissions;
    private final Double pueValue;
    private final CloudCarbonFootprintInstanceData cloudInstanceDetails;

    private CloudCarbonFootprintData() {
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

    public CloudCarbonFootprintInstanceData getCloudInstanceDetails() {
        return cloudInstanceDetails;
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

        List<String[]> csvLines = CSVParser.readAllCSVLinesExceptHeader(csvFile);
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
        CloudCarbonFootprintInstanceData cloudVMInstanceDetails;

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
        List<String[]> csvLines = CSVParser.readAllCSVLinesExceptHeader(instanceFileName);
        for (String[] lineFields : csvLines) {
            String csvInstanceType = lineFields[1].trim();
            if (csvInstanceType.equalsIgnoreCase(vmInstanceType.trim())) {
                cloudVMInstanceDetails.setInstanceVCpuCount(Double.parseDouble(lineFields[3].trim())); // Number of Instance vCPU
                cloudVMInstanceDetails.setPlatformTotalVcpu(Double.parseDouble(lineFields[5].trim())); // Number of Platform Total vCPU
                break;
            }
        }

        if (microarchitecture != null) {
            csvLines = CSVParser.readAllCSVLinesExceptHeader(coefficientsFileName);
            for (String[] lineFields : csvLines) {
                String csvMicroarchitecture = lineFields[1].trim();
                if (csvMicroarchitecture.equals(microarchitecture)) {
                    cloudVMInstanceDetails.setInstanceEnergyUsageIdle(Double.parseDouble(lineFields[2].trim())); // Instance Watt usage @ Idle
                    cloudVMInstanceDetails.setInstanceEnergyUsageFull(Double.parseDouble(lineFields[3].trim())); // Instance Watt usage @ 100%
                }
            }
        }
        return cloudVMInstanceDetails;
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

        List<String[]> csvLines = CSVParser.readAllCSVLinesExceptHeader("/instances/aws-instances.csv");
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

}