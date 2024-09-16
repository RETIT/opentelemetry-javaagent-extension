package io.retit.opentelemetry.javaagent.extension;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General configuration class for the opentelemetry agent extension.
 * This class is used to configure which data is collected and where the collected data is persisted.
 * For this purpose, System properties are used. Such System properties can also
 * be passed as environment variables.
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

    public static String getCloudProviderInstanceType() {
        return getStringProperty(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY);
    }

    public static String getMicroarchitecture() {
        return getStringProperty(Constants.RETIT_EMISSIONS_MICROARCHITECTURE_CONFIGURATION_PROPERTY);
    }

    public static String getStorageType() {
        return getProperty(Constants.RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY,
                String::valueOf, Constants.RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY_VALUE_SSD);
    }

    /**
     * Returns the property of type String with the given propertyName.
     *
     * @param propertyName - the property to return.
     * @return - the value of the property or the default defined in  Constants.RETIT_VALUE_NOT_SET.
     */
    public static String getStringProperty(final String propertyName) {
        return getProperty(propertyName, String::valueOf, Constants.RETIT_VALUE_NOT_SET);
    }

    /**
     * Returns the property of type boolean with the given propertyName.
     *
     * @param propertyName - the property to return.
     * @return - the value of the property or false
     */
    private static boolean getBooleanProperty(final String propertyName) {
        return getProperty(propertyName, Boolean::valueOf, false);
    }

    /**
     * Returns the property of type boolean with the given propertyName.
     *
     * @param propertyName - the property to return.
     * @param defaultValue - the defaultValue to return if the property is not set
     * @return - the value of the property or the provided defaultValue
     */
    private static boolean getBooleanProperty(final String propertyName, final boolean defaultValue) {
        return getProperty(propertyName, Boolean::valueOf, defaultValue);
    }

    private static <T> T getProperty(final String propertyName, final Converter<String, T> converter, final T defaultValue) {
        if (propertyName == null) {
            return defaultValue;
        }
        if (System.getProperty(propertyName) != null) {
            return converter.convert(System.getProperty(propertyName));
        }
        final String envVariableName = propertyName.toUpperCase(Locale.ENGLISH).replace(".", "_");
        if (System.getenv(envVariableName) != null) {
            return converter.convert(System.getenv(envVariableName));
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "RETIT APM: Property {0} not configured defaulting to {1}",
                    new Object[]{propertyName, (defaultValue != null ? defaultValue : "null")});
        }
        return defaultValue;
    }

    /**
     * Sets the given boolean property to the provided value.
     *
     * @param propertyName - the property name to set.
     * @param value        - the valueof the property.
     */
    public static void setBooleanProperty(final String propertyName, final boolean value) {
        System.setProperty(propertyName, Boolean.toString(value));
    }

    /**
     * Converts values of S to T.
     *
     * @param <S> Source type.
     * @param <T> Target type.
     */
    public interface Converter<S, T> {
        /**
         * Converts the given value of type S to type T.
         *
         * @param toConvert - the value to convert.
         * @return - the converted value.
         */
        T convert(S toConvert);
    }
}
