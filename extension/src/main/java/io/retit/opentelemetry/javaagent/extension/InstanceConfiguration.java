package io.retit.opentelemetry.javaagent.extension;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General agent configuration class.
 * <p>
 * This class is used by both Java EE agents as well as the host agent to
 * configure which data is collected and where the collected data is persisted.
 * For this purpose, System properties are used. Such System properties can also
 * be passed in a property file called retitapm.properties. If this file is
 * present on the classpath, it is read by the <code>InstanceConfiguration</code>
 * and evaluated accordingly.
 */
public class InstanceConfiguration {

    private static final Logger LOGGER = Logger.getLogger(InstanceConfiguration.class.getName());

    /**
     * Converts values of T1 to T2.
     *
     * @param <T1> Source type.
     * @param <T2> Target type.
     */
    public interface Converter<T1, T2> {
        T2 convert(T1 toConvert);
    }

    public static boolean isLogDiskDemand() {
        return getBooleanProperty(Constants.RETIT_APM_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogNetworkDemand() {
        return getBooleanProperty(Constants.RETIT_APM_NETWORK_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogResponseTime() {
        return getBooleanProperty(Constants.RETIT_APM_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY);
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

}
