package io.retit.opentelemetry.javaagent.extension;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General agent configuration class.
 * <p>
 * This class is used to configure which data is collected and where the collected data is persisted.
 * For this purpose, System properties are used. Such System properties can also
 * be passed in a property file called retitapm.properties. If this file is
 * present on the classpath, it is read by the <code>InstanceConfiguration</code>
 * and evaluated accordingly.
 */
public class InstanceConfiguration {

    private static final Logger LOGGER = Logger.getLogger(InstanceConfiguration.class.getName());

    public static boolean isLogCpuDemandDefaultTrue() {
        return getBooleanProperty(Constants.RETIT_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
    }

    public static boolean isLogHeapDemandDefaultTrue() {
        return getBooleanProperty(Constants.RETIT_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
    }

    public static boolean isLogGCEventDefaultTrue() {
        return getBooleanProperty(Constants.RETIT_GC_EVENT_LOGGING_CONFIGURATION_PROPERTY, true);
    }

    public static boolean isLogDiskDemand() {
        return getBooleanProperty(Constants.RETIT_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogNetworkDemand() {
        return getBooleanProperty(Constants.RETIT_NETWORK_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogResponseTime() {
        return getBooleanProperty(Constants.RETIT_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static String getCloudProvider() {
        return getStringProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY);
    }

    public static String getCloudProviderRegion() {
        return getStringProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY);
    }

    public static String getStorageType() {
        return getProperty(Constants.RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY,
                toConvert -> String.valueOf(toConvert), Constants.RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY_VALUE_SSD);
    }

    public static String getStringProperty(final String propertyName) {
        return getProperty(propertyName, toConvert -> String.valueOf(toConvert), "not-set");
    }

    public static boolean getBooleanProperty(final String propertyName) {
        return getProperty(propertyName, toConvert -> Boolean.valueOf(toConvert), false);
    }

    public static boolean getBooleanProperty(final String propertyName, boolean defaultValue) {
        return getProperty(propertyName, toConvert -> Boolean.valueOf(toConvert), defaultValue);
    }

    private static <T> T getProperty(final String propertyName, final Converter<String, T> converter, final T defaultValue) {
        if (propertyName == null) {
            return defaultValue;
        }
        if (System.getProperty(propertyName) != null) {
            return converter.convert(System.getProperty(propertyName));
        }
        final String envVariableName = propertyName.toUpperCase().replace(".", "_");
        if (System.getenv(envVariableName) != null) {
            return converter.convert(System.getenv(envVariableName));
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "RETIT APM: Property {0} not configured defaulting to {1}",
                    new Object[]{propertyName, (defaultValue != null ? defaultValue : "null")});
        }
        return defaultValue;
    }

    public static void setBooleanProperty(final String propertyName, boolean value) {
        System.setProperty(propertyName, Boolean.toString(value));
    }

    /**
     * Converts values of T1 to T2.
     *
     * @param <T1> Source type.
     * @param <T2> Target type.
     */
    public interface Converter<T1, T2> {
        T2 convert(T1 toConvert);
    }
}
