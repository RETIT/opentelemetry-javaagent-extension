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

package io.retit.opentelemetry.javaagent.extension.energy;

import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintCoefficients;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;

import java.util.logging.Logger;

/**
 * Wrapper class to calculate the energy in kWh required to process one GB over the network.
 */
public class NetworkEnergyData {

    private static final Logger LOGGER = Logger.getLogger(NetworkEnergyData.class.getName());

    private static final NetworkEnergyData INSTANCE = new NetworkEnergyData();

    private final double kwhPerGB;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private NetworkEnergyData() {
        // the CCF network coefficient is the energy per GB transferred, so it must not be converted to a time interval
        this.kwhPerGB = CloudCarbonFootprintCoefficients.NETWORK_KWH_PER_GB;

        LOGGER.info("Initialized NetworkEnergyData using following data: " + this);
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static NetworkEnergyData getInstance() {
        return INSTANCE;
    }

    public double getKwhPerGB() {
        return kwhPerGB;
    }

    @Override
    public String toString() {
        return "NetworkEnergyData{"
                + "kwhPerGB=" + kwhPerGB
                + '}';
    }
}
