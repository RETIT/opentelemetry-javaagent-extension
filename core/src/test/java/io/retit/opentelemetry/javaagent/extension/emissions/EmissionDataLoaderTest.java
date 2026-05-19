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
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.embodied.EmbodiedEmissions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmissionDataLoaderTest {

    @BeforeEach
    public void clearProperties() {
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_ON_PREMISE_TOTAL_EMBODIED_EMISSIONS_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_HARDWARE_LIFESPAN_CONFIGURATION_PROPERTY);
    }

    @Test
    public void testEmissionDataLoaderForOneCloudProvider() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "AWS");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "af-south-1");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY, "a1.medium");
        Assertions.assertNotNull(CloudCarbonFootprintData.getConfigInstance());
        double embodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInMilliGramPerMinute();
        Assertions.assertTrue(embodiedEmissions > 0.0);
    }

    @Test
    public void testEmissionDataLoaderForOnPremise() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "OnPremise");
        System.setProperty(Constants.RETIT_EMISSIONS_ON_PREMISE_TOTAL_EMBODIED_EMISSIONS_CONFIGURATION_PROPERTY, "3000.0");
        Assertions.assertNotNull(CloudCarbonFootprintData.getConfigInstance());
        double embodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInMilliGramPerMinute();
        Assertions.assertTrue(embodiedEmissions > 0.0);
    }

    @Test
    public void testHardwareLifespanDefaultValue() {
        // Test default value of 4 years
        double lifespanInYears = InstanceConfiguration.getHardwareLifespanInYears();
        Assertions.assertEquals(4.0, lifespanInYears);
    }

    @Test
    public void testHardwareLifespanCustomValue() {
        // Test custom lifespan value.
        System.setProperty(Constants.RETIT_EMISSIONS_HARDWARE_LIFESPAN_CONFIGURATION_PROPERTY, "8");
        double lifespanInYears = InstanceConfiguration.getHardwareLifespanInYears();
        Assertions.assertEquals(8.0, lifespanInYears);
    }

    @Test
    public void testHardwareLifespanNonPositiveFallsBackToDefault() {
        System.setProperty(Constants.RETIT_EMISSIONS_HARDWARE_LIFESPAN_CONFIGURATION_PROPERTY, "0");
        Assertions.assertEquals(4.0, InstanceConfiguration.getHardwareLifespanInYears());

        System.setProperty(Constants.RETIT_EMISSIONS_HARDWARE_LIFESPAN_CONFIGURATION_PROPERTY, "-2");
        Assertions.assertEquals(4.0, InstanceConfiguration.getHardwareLifespanInYears());
    }

    @Test
    public void testEmbodiedEmissionsWithDifferentLifespans() {
        // Test with default 4-year lifespan
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "OnPremise");
        System.setProperty(Constants.RETIT_EMISSIONS_ON_PREMISE_TOTAL_EMBODIED_EMISSIONS_CONFIGURATION_PROPERTY, "1000.0");
        
        double embodiedEmissionsDefault = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInMilliGramPerMinute();
        Assertions.assertTrue(embodiedEmissionsDefault > 0.0);

        // Clear and test with 8-year lifespan (should be half of default)
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_ON_PREMISE_TOTAL_EMBODIED_EMISSIONS_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_HARDWARE_LIFESPAN_CONFIGURATION_PROPERTY);
        
        System.setProperty(Constants.RETIT_EMISSIONS_HARDWARE_LIFESPAN_CONFIGURATION_PROPERTY, "8");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "OnPremise");
        System.setProperty(Constants.RETIT_EMISSIONS_ON_PREMISE_TOTAL_EMBODIED_EMISSIONS_CONFIGURATION_PROPERTY, "1000.0");
        
        double embodiedEmissions8Years = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInMilliGramPerMinute();
        
        // With 8-year lifespan, emissions should be half of 4-year lifespan.
        Assertions.assertEquals(embodiedEmissionsDefault / 2.0, embodiedEmissions8Years, 0.01);
    }
}
