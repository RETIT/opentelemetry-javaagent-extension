package io.retit.opentelemetry.javaagent.extension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Constants {
    public static final String RETIT_NAMESPACE = "de.retit";
    public static final String RETIT_DATASCOPE_PROPERTY = "de.retit.datascope";
    public static final String RETIT_APM_CASSANDRA_SERVER_HOSTNAME_PROPERTY = "de.retit.apm.cassandra.server.hostname";
    public static final String RETIT_APM_CASSANDRA_SERVER_PORT_PROPERTY = "de.retit.apm.cassandra.server.port";
    public static final String RETIT_APM_CASSANDRA_SERVER_USERNAME_PROPERTY = "de.retit.apm.cassandra.server.username";
    public static final String RETIT_APM_CASSANDRA_SERVER_PASSWORD_PROPERTY = "de.retit.apm.cassandra.server.password";
    public static final String RETIT_APM_CASSANDRA_SERVER_USESSL_PROPERTY = "de.retit.apm.cassandra.server.usessl";
    public static final String RETIT_APM_CASSANDRA_SERVER_TRUSTSTORE_FILE_PROPERTY = "de.retit.apm.cassandra.server.truststore.file";
    public static final String RETIT_APM_CASSANDRA_SERVER_TRUSTSTORE_PASSWORD_PROPERTY = "de.retit.apm.cassandra.server.truststore.password";
    public static final String RETIT_APM_CASSANDRA_SERVER_KEYSTORE_FILE_PROPERTY = "de.retit.apm.cassandra.server.keystore.file";
    public static final String RETIT_APM_CASSANDRA_SERVER_KEYSTORE_PASSWORD_PROPERTY = "de.retit.apm.cassandra.server.keystore.password";
    public static final String RETIT_APM_CASSANDRA_BATCH_SIZE_PROPERTY = "de.retit.apm.cassandra.batchsize";
    public static final String RETIT_APM_CASSANDRA_RESOURCE_DEMAND_BATCH_SIZE_PROPERTY = "de.retit.apm.cassandra.resource.demands.batchsize";
    public static final String RETIT_APM_CASSANDRA_MAXIMUM_SHARD_QUERY_PROPERTY = "de.retit.apm.cassandra.maximum.shard.query";
    public static final int RETIT_APM_CASSANDRA_MAXIMUM_SHARD_QUERY = 72;
    public static final String RETIT_APM_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.response.time";
    public static final String RETIT_APM_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.cpu.demand";
    public static final String RETIT_APM_TOTAL_CPU_TIMES_USED_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.total.cpu.time.used";
    public static final String RETIT_APM_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.heap.demand";
    public static final String RETIT_APM_TOTAL_HEAP_DEMAND_CONFIGURATION_PROPERTY = "de.retit.apm.log.total.heap.demand";
    public static final String RETIT_APM_NETWORK_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.network.demand";
    public static final String RETIT_APM_TOTAL_DISK_READ_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.total.disk.read.demand";
    public static final String RETIT_APM_TOTAL_DISK_WRITE_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.total.disk.write.demand";
    public static final String RETIT_APM_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.disk.demand";
    public static final String RETIT_APM_TOTAL_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.total.disk.demand";
    public static final String RETIT_APM_GC_EVENT_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.gc.event";
    public static final String RETIT_APM_THREAD_NAME_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.thread.name";
    public static final String RETIT_APM_NEWRELIC_ENABLED = "de.retit.apm.newrelic.enabled";
    // use underscores instead of dots for valid names in OpenShift configuration
    public static final String RETIT_WEB_CASSANDRA_HOST_PROPERTY = "de_retit_cassandra_host";
    public static final String RETIT_WEB_CASSANDRA_PORT_PROPERTY = "de_retit_cassandra_port";

    public static final String STARTCPUTIME_FIELD = "startcputime";
    public static final String ENDCPUTIME_FIELD = "endcputime";
    public static final String TOTALCPUTIMEUSED_FIELD = "totalcputimeused";

    public static final String STARTSYSTEMTIME_FIELD = "startsystemtime";
    public static final String ENDSYSTEMTIME_FIELD = "endsystemtime";

    public static final String STARTHEAPBYTEALLOCATION_FIELD = "startheapbyteallocation";
    public static final String ENDHEAPBYTEALLOCATION_FIELD = "endheapbyteallocation";
    public static final String TOTALHEAPDEMAND_FIELD = "totalheapbyteallocation";

    public static final String STARTDISKREADDEMAND_FIELD = "startdiskreaddemand";
    public static final String ENDDISKREADDEMAND_FIELD = "enddiskreaddemand";
    public static final String TOTALDISKREADDEMAND_FIELD = "totaldiskreaddemand";
    public static final String TOTALDISKDEMAND_FIELD = "totaldiskdemand";


    public static final String STARTDISKWRITEDEMAND_FIELD = "startdiskwritedemand";
    public static final String ENDDISKWRITEDEMAND_FIELD = "enddiskwritedemand";
    public static final String TOTALDISKWRITEDEMAND_FIELD = "totaldiskwritedemand";
    public static final String TOTAL_HEAP_SIZE_FIELD = "totalheapsize";
    public static final String START_THREAD_FIELD = "startthread";
    public static final String END_THREAD_FIELD = "endthread";
    public static final String LOG_SYSTEM_TIME_FIELD = "logsystemtime";

    public static final String MINOR_GC_OPERATION_NAME = "minor_free";
    public static final String MAJOR_GC_OPERATION_NAME = "major_free";
    public static final String GENERATOR_BRANCH_LIMIT = "de.retit.model.generator.branch.limit";
    public static final int GENERATOR_BRANCH_LIMIT_DEFAULT = 30;
    public static final String GENERATOR_DISABLE_IQR_BASED_RESOURCE_DEMANDS = "de.retit.model.generator.disable.iqr.based.resource.demands";
    public static final String GENERATOR_DISABLE_MEASUREMENT_DATA_AGGREGATION = "de.retit.model.generator.disable.measurement.data.aggregation";

    public static final String DYNATRACE_FAILED_TRANSACTIONS_ENABLED = "de.retit.data.sources.dynatrace.failed.transactions.enabled";
    public static final String DYNATRACE_IGNORE_HEADER_FOR_TRANSACTION_NAMES_ENABLED =
            "de.retit.data.sources.dynatrace.ignore.dynatraceheader.enabled";
    public static final String DYNATRACE_SEPERATE_JDBC_CALLS_DISABLED = "de.retit.data.sources.dynatrace.seperate.jdbc.calls.disabled";
    public static final String DYNATRACE_DISABLE_XML_REMOVAL = "de.retit.data.sources.dynatrace.xml.removal.disabled";
    public static final String DYNATRACE_FILTER_STATIC_CONTENT_DISABLED = "de.retit.data.sources.dynatrace.filter.static.content.disabled";
    public static final String DYNATRACE_FILTER_CLASSES_DISABLED = "de.retit.data.sources.dynatrace.filter.classes.disabled";
    public static final String DATASOURCES_ENABLED = "de_retit_enabled_datasources";
    public static final String STRIPE_API_PUBLIC_KEY = "STRIPE_API_PUBLIC_KEY";
    public static final String STRIPE_API_SECRET_KEY = "STRIPE_API_SECRET_KEY";
    public static final String STRIPE_API_ENDPOINT_SECRET = "STRIPE_API_ENDPOINT_SECRET";
    public static final String STRIPE_PLAN_MAX_ATTEMPTS = "STRIPE_PLAN_MAX_ATTEMPTS";
    public static final String STRIPE_PLAN_WAIT_TIME_SECONDS = "STRIPE_PLAN_WAIT_TIME";
    public static final int STRIPE_PLAN_MAX_ATTEMPTS_DEFAULT = 3;
    public static final Duration STRIPE_PLAN_WAIT_TIME_SECONDS_DEFAULT = Duration.of(2, ChronoUnit.SECONDS);

    public static final String KEYCLOAK_SERVER_URL_KEY = "KEYCLOAK_SERVER_URL";
    public static final String KEYCLOAK_CLIENT_SECRET_KEY = "KEYCLOAK_CLIENT_SECRET";

    // RETIT otel-agent-extension attribute names
    private static final String STRING_DECIMAL_FORMAT = "%s.%s";

    public static final String GC_LISTENER_NAME = "gclistener";
    public static final String SPAN_ATTRIBUTE_START_SYSTEM_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTSYSTEMTIME_FIELD);
    public static final String SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTHEAPBYTEALLOCATION_FIELD);
    public static final String SPAN_ATTRIBUTE_START_DISK_READ_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTDISKREADDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTDISKWRITEDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_START_CPU_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTCPUTIME_FIELD);
    public static final String SPAN_ATTRIBUTE_END_CPU_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDCPUTIME_FIELD);
    public static final String SPAN_ATTRIBUTE_END_DISK_READ_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDDISKREADDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDDISKWRITEDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDHEAPBYTEALLOCATION_FIELD);
    public static final String SPAN_ATTRIBUTE_END_SYSTEM_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDSYSTEMTIME_FIELD);
    public static final String SPAN_ATTRIBUTE_TOTAL_CPU_TIME_USED = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, TOTALCPUTIMEUSED_FIELD);
    public static final String SPAN_ATTRIBUTE_TOTAL_DISK_READ_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, TOTALDISKREADDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_TOTAL_DISK_WRITE_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, TOTALDISKWRITEDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_TOTAL_DISK_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, TOTALDISKDEMAND_FIELD);

    public static final String SPAN_ATTRIBUTE_TOTAL_HEAP_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, TOTALHEAPDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_LOG_SYSTEM_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, LOG_SYSTEM_TIME_FIELD);
    public static final String SPAN_ATTRIBUTE_TOTAL_HEAP_SIZE = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, TOTAL_HEAP_SIZE_FIELD);
    public static final String SPAN_ATTRIBUTE_SPAN_START_THREAD = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, START_THREAD_FIELD);
    public static final String SPAN_ATTRIBUTE_SPAN_END_THREAD = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, END_THREAD_FIELD);
    public static final String JAVA_AGENT_INSTRUMENTATION_NAME_GC_LISTENER = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, GC_LISTENER_NAME);

    public static final String GC_PREFIX = "gc";
    public static final String JAVA_AGENT_GC_OPERATION_NAME_MINOR_FREE =
            String.format("%s.%s.%s", RETIT_NAMESPACE, GC_PREFIX, MINOR_GC_OPERATION_NAME);
    public static final String JAVA_AGENT_GC_OPERATION_NAME_MAJOR_FREE =
            String.format("%s.%s.%s", RETIT_NAMESPACE, GC_PREFIX, MAJOR_GC_OPERATION_NAME);

    public static final String OPENTELEMETRY_IMPORTER_DELETE_IMPORTED_DATA = "de.retit.web.opentelemetry.deleteimporteddata";

    private Constants() {
    }

}
