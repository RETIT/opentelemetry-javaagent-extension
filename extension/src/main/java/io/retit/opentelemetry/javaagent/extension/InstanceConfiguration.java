package io.retit.opentelemetry.javaagent.extension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
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
 *

 */
public class InstanceConfiguration {

    private static final Logger LOGGER = Logger.getLogger(InstanceConfiguration.class.getName());

    private static final int DEFAULT_INT_PROPERTY_VALUE = 0;

    /**
     * Converts values of T1 to T2.
     *
     * @param <T1> Source type.
     * @param <T2> Target type.
     */
    public interface Converter<T1, T2> {
        T2 convert(T1 toConvert);
    }

    public static UUID getDatascope() {
        String datascope = getStringProperty(Constants.RETIT_DATASCOPE_PROPERTY);
        return UUID.fromString(datascope);
    }

    public static boolean isDisableIQRBasedResourceDemands() {
        return getBooleanProperty(Constants.GENERATOR_DISABLE_IQR_BASED_RESOURCE_DEMANDS);
    }

    public static boolean isDisableMeasurementDataAggregation() {
        return getBooleanProperty(Constants.GENERATOR_DISABLE_MEASUREMENT_DATA_AGGREGATION);
    }

    public static boolean isLogCPUDemand() {
        return getBooleanProperty(Constants.RETIT_APM_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogHeapDemand() {
        return getBooleanProperty(Constants.RETIT_APM_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogDiskDemand() {
        return getBooleanProperty(Constants.RETIT_APM_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogTotalDiskReadDemand() {
        return getBooleanProperty(Constants.RETIT_APM_TOTAL_DISK_READ_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogTotalDiskWriteDemand() {
        return getBooleanProperty(Constants.RETIT_APM_TOTAL_DISK_WRITE_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogTotalStorageDemand() {
        return getBooleanProperty(Constants.RETIT_APM_TOTAL_STORAGE_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogNetworkDemand() {
        return getBooleanProperty(Constants.RETIT_APM_NETWORK_DEMAND_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogResponseTime() {
        return getBooleanProperty(Constants.RETIT_APM_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogGCEvent() {
        return getBooleanProperty(Constants.RETIT_APM_GC_EVENT_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static boolean isLogThreadName() {
        return getBooleanProperty(Constants.RETIT_APM_THREAD_NAME_LOGGING_CONFIGURATION_PROPERTY);
    }

    public static String getRetitAPMCassandraHostname() {
        String result = getStringProperty(Constants.RETIT_APM_CASSANDRA_SERVER_HOSTNAME_PROPERTY);

        if (result == null) {
            return getStringProperty(Constants.RETIT_WEB_CASSANDRA_HOST_PROPERTY);
        }
        return result;
    }

    public static int getRetitAPMCassandraPort() {
        int result = getIntProperty(Constants.RETIT_APM_CASSANDRA_SERVER_PORT_PROPERTY);
        if (result == DEFAULT_INT_PROPERTY_VALUE) {
            result = getIntProperty(Constants.RETIT_WEB_CASSANDRA_PORT_PROPERTY);
        }
        return result;
    }

    public static String getRetitAPMCassandraUsername() {
        return getStringProperty(Constants.RETIT_APM_CASSANDRA_SERVER_USERNAME_PROPERTY);
    }

    public static String getRetitAPMCassandraPassword() {
        return getStringProperty(Constants.RETIT_APM_CASSANDRA_SERVER_PASSWORD_PROPERTY);
    }

    public static String getRetitAPMCassandraTruststoreFile() {
        return getStringProperty(Constants.RETIT_APM_CASSANDRA_SERVER_TRUSTSTORE_FILE_PROPERTY);
    }

    public static String getRetitAPMCassandraKeystoreFile() {
        return getStringProperty(Constants.RETIT_APM_CASSANDRA_SERVER_KEYSTORE_FILE_PROPERTY);
    }

    public static Boolean getRetitAPMCassandraUseSsl() {
        return getBooleanProperty(Constants.RETIT_APM_CASSANDRA_SERVER_USESSL_PROPERTY);
    }

    public static String getRetitAPMCassandraTruststorePassword() {
        return getStringProperty(Constants.RETIT_APM_CASSANDRA_SERVER_TRUSTSTORE_PASSWORD_PROPERTY);
    }

    public static String getRetitAPMCassandraKeystorePassword() {
        return getStringProperty(Constants.RETIT_APM_CASSANDRA_SERVER_KEYSTORE_PASSWORD_PROPERTY);
    }

    public static int getRetitAPMResourceDemandsBatchSize() {
        return getIntProperty(Constants.RETIT_APM_CASSANDRA_RESOURCE_DEMAND_BATCH_SIZE_PROPERTY);
    }

    public static int getRetitAPMCassandraBatchsize() {
        return getIntProperty(Constants.RETIT_APM_CASSANDRA_BATCH_SIZE_PROPERTY);
    }

    public static int getRetitAPMCassandraMaximumShardQuery() {
        int maximumShardQuery = getIntProperty(Constants.RETIT_APM_CASSANDRA_MAXIMUM_SHARD_QUERY_PROPERTY);
        return maximumShardQuery > 0 ? maximumShardQuery : Constants.RETIT_APM_CASSANDRA_MAXIMUM_SHARD_QUERY;
    }

    public static boolean isLogNewRelic() {
        return getBooleanProperty(Constants.RETIT_APM_NEWRELIC_ENABLED);
    }

    public static String getAvailableDataSources() {
        return getStringProperty(Constants.DATASOURCES_ENABLED);
    }

    public static boolean isDynatraceFilterStaticContentDisabled() {
        return getBooleanProperty(Constants.DYNATRACE_FILTER_STATIC_CONTENT_DISABLED);
    }

    public static boolean isDynatraceFilterClassesDisabled() {
        return getBooleanProperty(Constants.DYNATRACE_FILTER_CLASSES_DISABLED);
    }

    public static boolean isUseFailedTransactionsInDynatrace() {
        return getBooleanProperty(Constants.DYNATRACE_FAILED_TRANSACTIONS_ENABLED);
    }

    public static boolean isIgnoreDynatraceHeaderForTransactionNames() {
        return getBooleanProperty(Constants.DYNATRACE_IGNORE_HEADER_FOR_TRANSACTION_NAMES_ENABLED);
    }

    public static boolean isUseSeperateJDBCCallsInDynatraceDisabled() {
        return getBooleanProperty(Constants.DYNATRACE_SEPERATE_JDBC_CALLS_DISABLED);
    }

    public static boolean isDisableDynatraceXMLRemoval() {
        return getBooleanProperty(Constants.DYNATRACE_DISABLE_XML_REMOVAL);
    }

    public static int getBranchLimit() {
        Integer result = Integer.getInteger(Constants.GENERATOR_BRANCH_LIMIT);
        return (result != null) ? result : Constants.GENERATOR_BRANCH_LIMIT_DEFAULT;
    }

    public static String getStripeAPIPublicKey() {
        return getStringProperty(Constants.STRIPE_API_PUBLIC_KEY);
    }

    public static String getStripeAPISecretKey() {
        return getStringProperty(Constants.STRIPE_API_SECRET_KEY);
    }

    public static int getStripePlanMaxAttempts() {
        int result = getIntProperty(Constants.STRIPE_PLAN_MAX_ATTEMPTS);
        return (result != 0) ? result : Constants.STRIPE_PLAN_MAX_ATTEMPTS_DEFAULT;
    }

    public static Duration getStripePlanWaitTime() {
        int result = getIntProperty(Constants.STRIPE_PLAN_WAIT_TIME_SECONDS);
        return (result != 0) ? Duration.of(result, ChronoUnit.SECONDS) : Constants.STRIPE_PLAN_WAIT_TIME_SECONDS_DEFAULT;
    }

    public static String getStripeAPIEndpointSecret() {
        return getStringProperty(Constants.STRIPE_API_ENDPOINT_SECRET);
    }

    public static String getKeycloakServerUrl() {
        return getStringProperty(Constants.KEYCLOAK_SERVER_URL_KEY);
    }

    public static String getKeycloakClientSecret() {
        return getStringProperty(Constants.KEYCLOAK_CLIENT_SECRET_KEY);
    }

    public static boolean isOpentelemetryImporterDeleteImportedData() {
        return getProperty(Constants.OPENTELEMETRY_IMPORTER_DELETE_IMPORTED_DATA, toConvert -> Boolean.valueOf(toConvert), true);
    }

    public static int getIntProperty(String propertyName) {
        return getProperty(propertyName, toConvert -> Integer.valueOf(toConvert), DEFAULT_INT_PROPERTY_VALUE);
    }

    public static boolean getBooleanProperty(String propertyName) {
        return getProperty(propertyName, toConvert -> Boolean.valueOf(toConvert), false);
    }

    public static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        return getProperty(propertyName, toConvert -> Boolean.valueOf(toConvert), defaultValue);
    }

    public static String getStringProperty(String propertyName) {
        return getProperty(propertyName, toConvert -> toConvert, null);
    }

    private static <T> T getProperty(String propertyName, Converter<String, T> converter, T defaultValue) {
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

    public static void setBooleanProperty(String propertyName, boolean value) {
        System.setProperty(propertyName, Boolean.toString(value));
    }

}
