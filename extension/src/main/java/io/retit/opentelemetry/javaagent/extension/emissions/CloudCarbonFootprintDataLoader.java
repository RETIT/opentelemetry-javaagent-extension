/*
 *   Copyright 2024 RETIT GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.retit.opentelemetry.javaagent.extension.emissions;

import io.retit.opentelemetry.javaagent.extension.commons.CSVParser;
import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Loader responsible for loading cloud carbon footprint configuration data
 * from CSV files and configuration parameters. This class handles all
 * provider-specific initialization logic for grid emissions, instance details,
 * PUE values, and embodied emissions.
 */
final class CloudCarbonFootprintDataLoader {

    private static final double DOUBLE_ZERO = 0.0;

    private CloudCarbonFootprintDataLoader() {
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
    static Double initializeGridEmissionFactor(final String envRegion) {
        double gridEmissionFactorMetricTonPerKwh = 0.0;
        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            gridEmissionFactorMetricTonPerKwh = getDoubleValueFromCSVForRegionOrInstance("/ccf-coefficients/grid-emissions/grid-emissions-factors-aws.csv", 0, envRegion, 3);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            gridEmissionFactorMetricTonPerKwh = getDoubleValueFromCSVForRegionOrInstance("/ccf-coefficients/grid-emissions/grid-emissions-factors-azure.csv", 0, envRegion, 3);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            gridEmissionFactorMetricTonPerKwh = getDoubleValueFromCSVForRegionOrInstance("/ccf-coefficients/grid-emissions/grid-emissions-factors-gcp.csv", 0, envRegion, 2);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_ON_PREMISE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return InstanceConfiguration.getOnPremiseGridEmissionsFactor();
        }

        // we need to do the conversion using BigDecimal to avoid loosing precision
        BigDecimal metricTonPerKwh = BigDecimal.valueOf(gridEmissionFactorMetricTonPerKwh);
        BigDecimal conversionFactorToKKgperKwH = BigDecimal.valueOf(1_000.0);
        BigDecimal result = metricTonPerKwh.multiply(conversionFactorToKKgperKwH);

        return result.doubleValue(); // Convert to kilogram per kWh
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
     * @param microarchitecture the microarchitecture of the processor
     * @return the CloudCarbonFootprintInstanceData of the given instance type
     */
    static CloudCarbonFootprintInstanceData initializeCloudInstanceDetails(final String vmInstanceType, final String microarchitecture) {
        CloudCarbonFootprintInstanceData cloudVMInstanceDetails;

        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            cloudVMInstanceDetails = initializeCloudInstanceDetailsForAzure(vmInstanceType, microarchitecture);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            cloudVMInstanceDetails = initializeCloudInstanceDetailsForAws(vmInstanceType);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            cloudVMInstanceDetails = initializeCloudInstanceDetailsForGcp(vmInstanceType, microarchitecture);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_ON_PREMISE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            cloudVMInstanceDetails = initializeCloudInstanceDetailsForOnPremise();
        } else {
            cloudVMInstanceDetails = new CloudCarbonFootprintInstanceData(0.0, 0.0, 0.0, 0.0, 0.0);
        }

        cloudVMInstanceDetails.setTotalEmbodiedEmissions(getTotalEmbodiedEmissionsForInstanceType(vmInstanceType));

        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the PUE value based on the configured cloud provider.
     *
     * @return the PUE value for the configured cloud provider
     */
    static Double initializePueValue() {
        double returnValue = 0.0;
        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            returnValue = CloudCarbonFootprintCoefficients.AWS_PUE;
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            returnValue = CloudCarbonFootprintCoefficients.AZURE_PUE;
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            returnValue = CloudCarbonFootprintCoefficients.GCP_PUE;
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_ON_PREMISE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            returnValue = InstanceConfiguration.getOnPremisePue();
        }
        return returnValue;
    }

    /**
     * Returns the total embodied emissions for the specified instance type.
     * The total embodied emissions are determined based on the cloud provider and instance type.
     * It reads the embodied emissions from CSV files specific to each cloud provider.
     * The provided CSV files come from:
     * <a href="https://github.com/cloud-carbon-footprint/cloud-carbon-coefficients/tree/main/output">output</a>.
     * The embodied emissions are in kilograms of CO2e.
     *
     * @param instanceType the type of the instance for which the embodied emissions are to be initialized
     * @return the total embodied emissions in kilograms of CO2e
     */
    static Double getTotalEmbodiedEmissionsForInstanceType(final String instanceType) {
        if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return getDoubleValueFromCSVForRegionOrInstance("/ccf-coefficients/embodied-emissions/coefficients-aws-embodied.csv", 1, instanceType, 6);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return getDoubleValueFromCSVForRegionOrInstance("/ccf-coefficients/embodied-emissions/coefficients-gcp-embodied-mean.csv", 1, instanceType, 2);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return getDoubleValueFromCSVForRegionOrInstance("/ccf-coefficients/embodied-emissions/coefficients-azure-embodied.csv", 2, instanceType, 8);
        } else if (Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_ON_PREMISE.equalsIgnoreCase(InstanceConfiguration.getCloudProvider())) {
            return InstanceConfiguration.getOnPremiseTotalEmbodiedEmissions();
        }
        return 0.0;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for GCP instances.
     *
     * @param vmInstanceType - the VM instance type
     * @param microarchitecture - the processor microarchitecture
     * @return the CloudCarbonFootprintInstanceData of the given instance type
     */
    private static CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsForGcp(final String vmInstanceType, final String microarchitecture) {

        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = initializeCloudInstanceDetailsCommon("/ccf-coefficients/instances/gcp-instances.csv", "/ccf-coefficients/instances/coefficients-gcp-use.csv", vmInstanceType, microarchitecture);
        cloudVMInstanceDetails.setCloudProvider(CloudProvider.GCP);
        if (cloudVMInstanceDetails.getCpuPowerConsumptionIdle() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setCpuPowerConsumptionIdle(CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_GCP);
        }

        if (cloudVMInstanceDetails.getCpuPowerConsumption100Percent() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setCpuPowerConsumption100Percent(CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_GCP);
        }

        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for Azure instances.
     *
     * @param vmInstanceType - the VM instance type
     * @param microarchitecture - the processor microarchitecture
     * @return the CloudCarbonFootprintInstanceData of the given instance type
     */
    private static CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsForAzure(final String vmInstanceType, final String microarchitecture) {

        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = initializeCloudInstanceDetailsCommon("/ccf-coefficients/instances/azure-instances.csv", "/ccf-coefficients/instances/coefficients-azure-use.csv", vmInstanceType, microarchitecture);
        cloudVMInstanceDetails.setCloudProvider(CloudProvider.AZURE);
        if (cloudVMInstanceDetails.getCpuPowerConsumptionIdle() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setCpuPowerConsumptionIdle(CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AZURE);
        }

        if (cloudVMInstanceDetails.getCpuPowerConsumption100Percent() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setCpuPowerConsumption100Percent(CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AZURE);
        }

        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for Azure and GCP instances
     * as AWS has a different file format.
     *
     * @param instanceFileName                           - file name containing the instance details for the current cloud provider.
     * @param microArchitecturePowerCoefficientsFileName - file name containing the power coefficients details for the given processor microarchitecture.
     * @param vmInstanceType                             - the instance type.
     * @param microarchitecture                          - the processor microarchitecture.
     * @return the CloudCarbonFootprintInstanceData of the given instance type
     */
    private static CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsCommon(final String instanceFileName, final String microArchitecturePowerCoefficientsFileName, final String vmInstanceType, final String microarchitecture) {
        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = new CloudCarbonFootprintInstanceData();
        cloudVMInstanceDetails.setInstanceType(vmInstanceType);
        List<String[]> csvLines = CSVParser.readAllCSVLinesExceptHeader(instanceFileName);
        for (String[] lineFields : csvLines) {
            String csvInstanceType = lineFields[1].trim();
            if (csvInstanceType.equalsIgnoreCase(vmInstanceType.trim())) {
                cloudVMInstanceDetails.setInstanceVCpuCount(Double.parseDouble(lineFields[3].trim())); // Number of Instance vCPU
                cloudVMInstanceDetails.setPlatformTotalVCpuCount(Double.parseDouble(lineFields[5].trim())); // Number of Platform Total vCPU
                break;
            }
        }

        if (microarchitecture != null) {
            csvLines = CSVParser.readAllCSVLinesExceptHeader(microArchitecturePowerCoefficientsFileName);
            for (String[] lineFields : csvLines) {
                String csvMicroarchitecture = lineFields[1].trim();
                if (csvMicroarchitecture.equals(microarchitecture)) {
                    cloudVMInstanceDetails.setCpuPowerConsumptionIdle(Double.parseDouble(lineFields[2].trim())); // Instance Watt usage @ Idle
                    cloudVMInstanceDetails.setCpuPowerConsumption100Percent(Double.parseDouble(lineFields[3].trim())); // Instance Watt usage @ 100%
                }
            }
        }
        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the instance details (number of cpus and their power consumption) for AWS instances.
     * As power consumption is included in the aws instance file a separate method is used compared to Azure and GCP.
     *
     * @param vmInstanceType - the VM instance type
     * @return the CloudCarbonFootprintInstanceData of the given instance type
     */
    private static CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsForAws(final String vmInstanceType) {

        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = new CloudCarbonFootprintInstanceData();
        cloudVMInstanceDetails.setCloudProvider(CloudProvider.AWS);
        cloudVMInstanceDetails.setInstanceType(vmInstanceType);
        List<String[]> csvLines = CSVParser.readAllCSVLinesExceptHeader("/ccf-coefficients/instances/aws-instances.csv");
        for (String[] lineFields : csvLines) {
            String csvInstanceType = lineFields[0];
            if (csvInstanceType.equalsIgnoreCase(vmInstanceType.trim())) {
                cloudVMInstanceDetails.setInstanceVCpuCount(Double.parseDouble(lineFields[2])); // Instance vCPU
                cloudVMInstanceDetails.setPlatformTotalVCpuCount(Double.parseDouble(lineFields[3])); // Platform Total Number of vCPU
                cloudVMInstanceDetails.setCpuPowerConsumptionIdle(Double.parseDouble(lineFields[27].replace("\"", "").trim().replace(',', '.'))); // Instance Watt usage @ Idle
                cloudVMInstanceDetails.setCpuPowerConsumption100Percent(Double.parseDouble(lineFields[30].replace("\"", "").trim().replace(',', '.'))); // Instance Watt usage @ 100%
            }
        }

        if (cloudVMInstanceDetails.getCpuPowerConsumptionIdle() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setCpuPowerConsumptionIdle(CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AWS);
        }

        if (cloudVMInstanceDetails.getCpuPowerConsumption100Percent() == DOUBLE_ZERO) {
            cloudVMInstanceDetails.setCpuPowerConsumption100Percent(CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AWS);
        }

        return cloudVMInstanceDetails;
    }

    /**
     * Initializes the instance details for on-premise instances.
     * All values are provided through configuration parameters.
     *
     * @return the CloudCarbonFootprintInstanceData with values from configuration
     */
    private static CloudCarbonFootprintInstanceData initializeCloudInstanceDetailsForOnPremise() {
        CloudCarbonFootprintInstanceData cloudVMInstanceDetails = new CloudCarbonFootprintInstanceData();
        cloudVMInstanceDetails.setCloudProvider(CloudProvider.ON_PREMISE);
        cloudVMInstanceDetails.setInstanceType("OnPremise");
        cloudVMInstanceDetails.setInstanceVCpuCount(InstanceConfiguration.getOnPremiseInstanceVCpuCount());
        cloudVMInstanceDetails.setPlatformTotalVCpuCount(InstanceConfiguration.getOnPremisePlatformTotalVCpuCount());
        cloudVMInstanceDetails.setCpuPowerConsumptionIdle(InstanceConfiguration.getOnPremiseCpuPowerConsumptionIdle());
        cloudVMInstanceDetails.setCpuPowerConsumption100Percent(InstanceConfiguration.getOnPremiseCpuPowerConsumption100());
        return cloudVMInstanceDetails;
    }

    private static double getDoubleValueFromCSVForRegionOrInstance(final String csvFile, final int instanceTypeOrRegionCsvField, final String instanceTypeOrRegion, final int csvField) {

        List<String[]> csvLines = CSVParser.readAllCSVLinesExceptHeader(csvFile);
        for (String[] lineFields : csvLines) {
            String csvInstance = lineFields[instanceTypeOrRegionCsvField].trim();
            if (csvInstance.equalsIgnoreCase(instanceTypeOrRegion.trim())) {
                return Double.parseDouble(lineFields[csvField].trim());
            }
        }

        return 0.0;
    }
}

