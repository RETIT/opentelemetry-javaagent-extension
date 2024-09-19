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

package io.retit.opentelemetry.javaagent.extension.emissions.embodied;

import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

/**
 * The {@code EmbodiedEmissions} class calculates the embodied carbon emissions associated with the usage of CPU resources.
 * Embodied emissions refer to the carbon footprint incurred during the manufacturing and provisioning of computing resources.
 */
public class EmbodiedEmissions {

    private static final EmbodiedEmissions INSTANCE = new EmbodiedEmissions();
    private final CloudCarbonFootprintData configLoader;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration for emissions calculations.
     */
    private EmbodiedEmissions() {
        configLoader = CloudCarbonFootprintData.getConfigInstance();
    }

    /**
     * Provides a global access point to the {@code EmbodiedEmissions} instance, implementing a singleton pattern.
     * This ensures that only one instance of the class is created and used throughout the application.
     *
     * @return The single instance of {@code EmbodiedEmissions}.
     */
    public static EmbodiedEmissions getInstance() {
        return INSTANCE;
    }

    /**
     * Calculates the embodied carbon emissions in grams in minutes.
     * This method estimates the emissions based on the cloud instance's specifications and the total embodied emissions data.
     *
     * @return The calculated embodied carbon emissions in milligrams.
     */
    public double calculateEmbodiedEmissionsInMilliGramPerMinute() {
        if (InstanceConfiguration.getCloudProviderInstanceType() == null || Constants.RETIT_VALUE_NOT_SET.equals(InstanceConfiguration.getCloudProviderInstanceType())) {
            return 0;
        } else {
            return configLoader.getCloudInstanceDetails().getTotalEmbodiedEmissions() * (CloudCarbonFootprintCoefficients.TOTAL_EMBODIED_EMISSIONS_TO_GRAMS_PER_HOUR / 60)
                    * (configLoader.getCloudInstanceDetails().getInstanceVCpuCount() / configLoader.getCloudInstanceDetails().getPlatformTotalVCpuCount()) * 1_000;
        }
    }
}