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

/**
 * Class containing emission coefficients for different cloud resources.
 * These coefficients follow the approach of the Cloud Carbon Footprint project outlined here:
 * https://www.cloudcarbonfootprint.org/docs/methodology
 */
public class CloudCarbonFootprintCoefficients {

    /**
     * HDD storage energy consumption in Wh per TB per hour.
     * Source: https://www.cloudcarbonfootprint.org/docs/methodology#appendix-i-energy-coefficients
     */
    public static final double STORAGE_ENERGY_CONSUMPTION_WH_HDD_PER_TB_HOUR = 0.65;

    /**
     * SSD storage energy consumption in Wh per TB per hour.
     * Source: https://www.cloudcarbonfootprint.org/docs/methodology#appendix-i-energy-coefficients
     */
    public static final double STORAGE_ENERGY_CONSUMPTION_WH_SSD_PER_TB_HOUR = 1.2;

    /**
     * Average minimum watt consumption for AWS.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    public static final double AVERAGE_MIN_WATT_AWS = 1.14;

    /**
     * Average maximum watt consumption for AWS.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    public static final double AVERAGE_MAX_WATT_AWS = 4.34;

    /**
     * Average minimum watt consumption for Azure.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    public static final double AVERAGE_MIN_WATT_AZURE = 0.85;

    /**
     * Average maximum watt consumption for Azure.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    public static final double AVERAGE_MAX_WATT_AZURE = 3.69;

    /**
     * Average minimum watt consumption for GCP.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    public static final double AVERAGE_MIN_WATT_GCP = 0.68;

    /**
     * Average maximum watt consumption for GCP.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    public static final double AVERAGE_MAX_WATT_GCP = 3.77;

    /**
     * Coefficient for breaking down total kg embodied emissions to gram per hour based on four year usage.
     * This is calculated as follows: (1000 (kg to g) / 4 (years) / 12 (months per year) / 30 (days per month) / 24 (hours per day).
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/embodied-emissions">...</a>
     */
    public static final double TOTAL_EMBODIED_EMISSIONS_TO_GRAMS_PER_HOUR = 0.0289;

    /**
     * Coefficient for calculating memory emissions in kWh per GB-hour.
     * It is the same for all cloud providers.
     * Source: https://www.cloudcarbonfootprint.org/docs/methodology/#appendix-i-energy-coefficients
     */
    public static final double MEMORY_KWH_PER_GB_HOUR = 0.000_392;

    /**
     * Coefficient for calculating network emissions in kWh per GB-hour.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#networking">...</a>
     */
    public static final double NETWORK_KWH_PER_GB_HOUR =  0.001;

    /**
     * Coefficient for Power Usage Effectiveness (PUE) for AWS.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#power-usage-effectiveness">...</a>
     */
    public static final double AWS_PUE = 1.135;

    /**
     * Coefficient for Power Usage Effectiveness (PUE) for Azure.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#power-usage-effectiveness">...</a>
     */
    public static final double AZURE_PUE = 1.185;

    /**
     * Coefficient for Power Usage Effectiveness (PUE) for GCP.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#power-usage-effectiveness">...</a>
     */
    public static final double GCP_PUE = 1.1;
}
