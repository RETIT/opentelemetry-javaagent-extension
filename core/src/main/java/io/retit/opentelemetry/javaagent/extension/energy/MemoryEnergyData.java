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
 * Wrapper class to calculate the energy in kWh required for one GB of main memory.
 */
public class MemoryEnergyData {

    private static final Logger LOGGER = Logger.getLogger(MemoryEnergyData.class.getName());

    private static final MemoryEnergyData INSTANCE = new MemoryEnergyData();

    private final double kwhPerGBMinute;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the {@link CloudCarbonFootprintData} to load necessary configuration.
     */
    private MemoryEnergyData() {
        // convert to one minute
        this.kwhPerGBMinute = CloudCarbonFootprintCoefficients.MEMORY_KWH_PER_GB_HOUR / 60;

        LOGGER.info("Initialized MemoryEnergyData using following data: " + this);
    }

    /**
     * Provides a global access point to the {@code CpuEmissions} instance, implementing a singleton pattern.
     *
     * @return The single instance of {@code CpuEmissions}.
     */
    public static MemoryEnergyData getInstance() {
        return INSTANCE;
    }

    public double getKwhPerGBMinute() {
        return kwhPerGBMinute;
    }

    @Override
    public String toString() {
        return "MemoryEnergyData{"
                + "kwhPerGBMinute=" + kwhPerGBMinute
                + '}';
    }
}
