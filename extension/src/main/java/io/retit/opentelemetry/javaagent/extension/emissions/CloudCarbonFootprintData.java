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

import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;

/**
 * Holds the cloud carbon footprint configuration data for the current instance.
 * Initialization logic is delegated to {@link CloudCarbonFootprintDataLoader}.
 */
public final class CloudCarbonFootprintData {

    private static final CloudCarbonFootprintData CONFIG_INSTANCE = new CloudCarbonFootprintData();

    private Double gridEmissionsFactor;
    private Double pueValue;
    private CloudCarbonFootprintInstanceData cloudInstanceDetails;

    private CloudCarbonFootprintData() {
        init();
    }

    /**
     * Returns the singleton instance of CloudCarbonFootprintData.
     *
     * @return the singleton instance of CloudCarbonFootprintData
     */
    public static CloudCarbonFootprintData getConfigInstance() {
        return CONFIG_INSTANCE;
    }

    void init() {
        String microarchitecture = InstanceConfiguration.getMicroarchitecture();
        this.gridEmissionsFactor = CloudCarbonFootprintDataLoader.initializeGridEmissionFactor(InstanceConfiguration.getCloudProviderRegion());
        this.cloudInstanceDetails = CloudCarbonFootprintDataLoader.initializeCloudInstanceDetails(InstanceConfiguration.getCloudProviderInstanceType(), microarchitecture);
        this.pueValue = CloudCarbonFootprintDataLoader.initializePueValue();
    }

    public Double getGridEmissionsFactor() {
        return gridEmissionsFactor;
    }

    public Double getPueValue() {
        return pueValue;
    }

    public CloudCarbonFootprintInstanceData getCloudInstanceDetails() {
        return cloudInstanceDetails;
    }

}