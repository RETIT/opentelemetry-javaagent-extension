package io.retit.opentelemetry.javaagent.extension.emissions;

/**
 * Class containing emission coefficients for different cloud resources.
 * These coefficients follow the approach of the Cloud Carbon Footprint project.
 */
public class EmissionCoefficients {

    /**
     * HDD storage emissions per TB per hour.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#storage">...</a>
     */
    public static final double STORAGE_EMISSIONS_HDD_PER_TB_HOUR = 0.00065;

    /**
     * SSD storage emissions per TB per hour.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#storage">...</a>
     */
    public static final double STORAGE_EMISSIONS_SSD_PER_TB_HOUR = 0.0012;

    /**
     * Average minimum watt consumption for AWS.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    double AVERAGE_MIN_WATT_AWS = 1.14;

    /**
     * Average maximum watt consumption for AWS.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    double AVERAGE_MAX_WATT_AWS = 4.34;

    /**
     * Average minimum watt consumption for Azure.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    double AVERAGE_MIN_WATT_AZURE = 0.85;

    /**
     * Average maximum watt consumption for Azure.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    double AVERAGE_MAX_WATT_AZURE = 3.69;

    /**
     * Average minimum watt consumption for GCP.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    double AVERAGE_MIN_WATT_GCP = 0.68;

    /**
     * Average maximum watt consumption for GCP.
     * Source: <a href="https://deepnote.com/launch?url=https://github.com/davidmytton/cloud-carbon-coefficients/blob/main/coefficients.ipynb">...</a>
     * The values are calculated in the Jupyter notebook, but are not added to the Github repo.
     * They're also provided in <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#compute">...</a>, but havent seem to be updated there
     */
    double AVERAGE_MAX_WATT_GCP = 3.77;

    /**
     * Coefficient for breaking down total kg embodied emissions to gram per hour (1000/4/30/24) based on four year usage.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/embodied-emissions">...</a>
     */
    double EMBODIED_EMISSIONS_CONVERSION_TO_GRAMS_PER_HOUR_FACTOR = 0.0289;

    /**
     * Coefficient for calculating memory emissions in kWh per GB.
     * Source: <a href="https://www.cloudcarbonfootprint.org/docs/methodology/#memory">...</a>
     */
    double MEMORY_KWH_PER_GB = 0.000392;
}
