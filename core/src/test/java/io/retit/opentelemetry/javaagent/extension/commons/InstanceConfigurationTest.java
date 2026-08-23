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

package io.retit.opentelemetry.javaagent.extension.commons;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InstanceConfigurationTest {

    @BeforeEach
    @AfterEach
    public void clearProperties() {
        System.clearProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_EMISSIONS_ON_PREMISE_PUE_CONFIGURATION_PROPERTY);
        System.clearProperty(Constants.RETIT_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    @Test
    public void testUnquotedStringPropertyIsReturnedUnchanged() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "eu-central-1");
        Assertions.assertEquals("eu-central-1", InstanceConfiguration.getCloudProviderRegion());
    }

    @Test
    public void testEmbeddingQuotesAreRemovedFromStringProperty() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "\"eu-central-1\"");
        Assertions.assertEquals("eu-central-1", InstanceConfiguration.getCloudProviderRegion());
    }

    @Test
    public void testEmbeddingQuotesAreRemovedFromNumericProperty() {
        System.setProperty(Constants.RETIT_EMISSIONS_ON_PREMISE_PUE_CONFIGURATION_PROPERTY, "\"1.25\"");
        Assertions.assertEquals(1.25, InstanceConfiguration.getOnPremisePue(), 0.0);
    }

    @Test
    public void testEmbeddingQuotesAreRemovedFromBooleanProperty() {
        System.setProperty(Constants.RETIT_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY, "\"false\"");
        Assertions.assertFalse(InstanceConfiguration.isLogCpuDemandDefaultTrue());
    }

    @Test
    public void testSingleQuoteCharacterIsNotRemoved() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "\"");
        Assertions.assertEquals("\"", InstanceConfiguration.getCloudProviderRegion());
    }

    @Test
    public void testOnlyLeadingOrTrailingQuoteIsNotRemoved() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "\"eu-central-1");
        Assertions.assertEquals("\"eu-central-1", InstanceConfiguration.getCloudProviderRegion());

        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "eu-central-1\"");
        Assertions.assertEquals("eu-central-1\"", InstanceConfiguration.getCloudProviderRegion());
    }

    @Test
    public void testUnsetPropertyStillReturnsDefault() {
        Assertions.assertEquals(Constants.RETIT_VALUE_NOT_SET, InstanceConfiguration.getCloudProviderRegion());
        Assertions.assertEquals(1.43, InstanceConfiguration.getOnPremisePue(), 0.0);
    }
}
