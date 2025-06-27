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

import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CloudCarbonFootprintDataTest {

    @BeforeEach
    public void clearProperties() {
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_MICROARCHITECTURE_CONFIGURATION_PROPERTY);
    }

    @Test
    public void testCloudCarbonFootprintDataAWS() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "AWS");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "eu-central-1");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY, "t2.xlarge");
        CloudCarbonFootprintData instance = CloudCarbonFootprintData.getConfigInstance();
        instance.init();
        Assertions.assertNotNull(instance);
        Assertions.assertEquals(0.37995, instance.getGridEmissionsFactor());
        CloudCarbonFootprintInstanceData cloudCarbonFootprintInstanceData = instance.getCloudInstanceDetails();
        Assertions.assertNotNull(cloudCarbonFootprintInstanceData);

        Assertions.assertEquals(4, cloudCarbonFootprintInstanceData.getInstanceVCpuCount());
        Assertions.assertEquals(72, cloudCarbonFootprintInstanceData.getPlatformTotalVCpuCount());
        Assertions.assertEquals(8.4, cloudCarbonFootprintInstanceData.getCpuPowerConsumptionIdle());
        Assertions.assertEquals(28.4, cloudCarbonFootprintInstanceData.getCpuPowerConsumption100Percent());
        Assertions.assertEquals(1477.54, cloudCarbonFootprintInstanceData.getTotalEmbodiedEmissions());
        Assertions.assertEquals(CloudProvider.AWS, cloudCarbonFootprintInstanceData.getCloudProvider());
    }

    @Test
    public void testCloudCarbonFootprintDataGCP() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "GCP");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "europe-west3");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY, "e2-standard-4");
        CloudCarbonFootprintData instance = CloudCarbonFootprintData.getConfigInstance();
        instance.init();
        Assertions.assertNotNull(instance);
        Assertions.assertEquals(0.37995, instance.getGridEmissionsFactor());
        CloudCarbonFootprintInstanceData cloudCarbonFootprintInstanceData = instance.getCloudInstanceDetails();
        Assertions.assertNotNull(cloudCarbonFootprintInstanceData);
        Assertions.assertEquals(4, cloudCarbonFootprintInstanceData.getInstanceVCpuCount());
        Assertions.assertEquals(32, cloudCarbonFootprintInstanceData.getPlatformTotalVCpuCount());
        Assertions.assertEquals(CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_GCP, cloudCarbonFootprintInstanceData.getCpuPowerConsumptionIdle());
        Assertions.assertEquals(CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_GCP, cloudCarbonFootprintInstanceData.getCpuPowerConsumption100Percent());
        Assertions.assertEquals(1230.46, cloudCarbonFootprintInstanceData.getTotalEmbodiedEmissions());
        Assertions.assertEquals(CloudProvider.GCP, cloudCarbonFootprintInstanceData.getCloudProvider());
    }

    @Test
    public void testCloudCarbonFootprintDataGCPWithMicroarchitecture() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "GCP");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "europe-west3");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY, "e2-standard-4");
        System.setProperty(Constants.RETIT_EMISSIONS_MICROARCHITECTURE_CONFIGURATION_PROPERTY, "Haswell");
        CloudCarbonFootprintData instance = CloudCarbonFootprintData.getConfigInstance();
        instance.init();
        Assertions.assertNotNull(instance);
        Assertions.assertEquals(0.37995, instance.getGridEmissionsFactor());
        CloudCarbonFootprintInstanceData cloudCarbonFootprintInstanceData = instance.getCloudInstanceDetails();
        Assertions.assertNotNull(cloudCarbonFootprintInstanceData);
        Assertions.assertEquals(4, cloudCarbonFootprintInstanceData.getInstanceVCpuCount());
        Assertions.assertEquals(32, cloudCarbonFootprintInstanceData.getPlatformTotalVCpuCount());
        Assertions.assertEquals(1.9005681818181814, cloudCarbonFootprintInstanceData.getCpuPowerConsumptionIdle());
        Assertions.assertEquals(5.9688982156043195, cloudCarbonFootprintInstanceData.getCpuPowerConsumption100Percent());
        Assertions.assertEquals(1230.46, cloudCarbonFootprintInstanceData.getTotalEmbodiedEmissions());
        Assertions.assertEquals(CloudProvider.GCP, cloudCarbonFootprintInstanceData.getCloudProvider());
    }

    @Test
    public void testCloudCarbonFootprintDataAzure() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "Azure");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "West Europe");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY, "A1 v2");
        CloudCarbonFootprintData instance = CloudCarbonFootprintData.getConfigInstance();
        instance.init();
        Assertions.assertNotNull(instance);
        Assertions.assertEquals(0.30901, instance.getGridEmissionsFactor());
        CloudCarbonFootprintInstanceData cloudCarbonFootprintInstanceData = instance.getCloudInstanceDetails();
        Assertions.assertNotNull(cloudCarbonFootprintInstanceData);
        Assertions.assertEquals(1, cloudCarbonFootprintInstanceData.getInstanceVCpuCount());
        Assertions.assertEquals(8, cloudCarbonFootprintInstanceData.getPlatformTotalVCpuCount());
        Assertions.assertEquals(CloudCarbonFootprintCoefficients.AVERAGE_MIN_WATT_AZURE, cloudCarbonFootprintInstanceData.getCpuPowerConsumptionIdle());
        Assertions.assertEquals(CloudCarbonFootprintCoefficients.AVERAGE_MAX_WATT_AZURE, cloudCarbonFootprintInstanceData.getCpuPowerConsumption100Percent());
        Assertions.assertEquals(1216.62, cloudCarbonFootprintInstanceData.getTotalEmbodiedEmissions());
        Assertions.assertEquals(CloudProvider.AZURE, cloudCarbonFootprintInstanceData.getCloudProvider());
    }

    @Test
    public void testCloudCarbonFootprintDataAzureWithMicroarchitecture() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "Azure");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "West Europe");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY, "HB120-96rs v3");
        System.setProperty(Constants.RETIT_EMISSIONS_MICROARCHITECTURE_CONFIGURATION_PROPERTY, "EPYC 3rd Gen");
        CloudCarbonFootprintData instance = CloudCarbonFootprintData.getConfigInstance();
        instance.init();
        Assertions.assertNotNull(instance);
        Assertions.assertEquals(0.30901, instance.getGridEmissionsFactor());
        CloudCarbonFootprintInstanceData cloudCarbonFootprintInstanceData = instance.getCloudInstanceDetails();
        Assertions.assertNotNull(cloudCarbonFootprintInstanceData);
        Assertions.assertEquals(0.8, cloudCarbonFootprintInstanceData.getInstanceVCpuCount());
        Assertions.assertEquals(120, cloudCarbonFootprintInstanceData.getPlatformTotalVCpuCount());
        Assertions.assertEquals(0.44538981119791665, cloudCarbonFootprintInstanceData.getCpuPowerConsumptionIdle());
        Assertions.assertEquals(2.0193277994791665, cloudCarbonFootprintInstanceData.getCpuPowerConsumption100Percent());
        Assertions.assertEquals(1699.62, cloudCarbonFootprintInstanceData.getTotalEmbodiedEmissions());
        Assertions.assertEquals(CloudProvider.AZURE, cloudCarbonFootprintInstanceData.getCloudProvider());
    }
}
