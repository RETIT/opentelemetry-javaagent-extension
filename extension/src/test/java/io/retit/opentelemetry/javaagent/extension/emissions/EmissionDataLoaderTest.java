package io.retit.opentelemetry.javaagent.extension.emissions;

import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.emissions.cpu.EmbodiedEmissions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmissionDataLoaderTest {

    @Test
    public void testEmissionDataLoaderForOneCloudProvider() {
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY, "AWS");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY, "af-south-1");
        System.setProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY, "a1.medium");
        Assertions.assertNotNull(EmissionDataLoader.getConfigInstance());
        double embodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInMilliGram(10);
        Assertions.assertTrue(embodiedEmissions > 0.0);
    }
}
