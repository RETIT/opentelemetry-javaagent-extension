package io.retit.opentelemetry.javaagent.extension;

public class Constants {
    public static final String RETIT_NAMESPACE = "de.retit";
    public static final String RETIT_APM_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.response.time";
    public static final String RETIT_APM_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.cpu.demand";
    public static final String RETIT_APM_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.heap.demand";
    public static final String RETIT_APM_NETWORK_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.network.demand";
    public static final String RETIT_APM_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.disk.demand";
    public static final String RETIT_APM_GC_EVENT_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.gc.event";
    public static final String RETIT_APM_THREAD_NAME_LOGGING_CONFIGURATION_PROPERTY = "de.retit.apm.log.thread.name";

    public static final String STARTCPUTIME_FIELD = "startcputime";
    public static final String ENDCPUTIME_FIELD = "endcputime";

    public static final String STARTSYSTEMTIME_FIELD = "startsystemtime";
    public static final String ENDSYSTEMTIME_FIELD = "endsystemtime";

    public static final String STARTHEAPBYTEALLOCATION_FIELD = "startheapbyteallocation";
    public static final String ENDHEAPBYTEALLOCATION_FIELD = "endheapbyteallocation";

    public static final String STARTDISKREADDEMAND_FIELD = "startdiskreaddemand";
    public static final String ENDDISKREADDEMAND_FIELD = "enddiskreaddemand";

    public static final String STARTDISKWRITEDEMAND_FIELD = "startdiskwritedemand";
    public static final String ENDDISKWRITEDEMAND_FIELD = "enddiskwritedemand";
    public static final String TOTAL_HEAP_SIZE_FIELD = "totalheapsize";
    public static final String START_THREAD_FIELD = "startthread";
    public static final String END_THREAD_FIELD = "endthread";
    public static final String LOG_SYSTEM_TIME_FIELD = "logsystemtime";

    public static final String MINOR_GC_OPERATION_NAME = "minor_free";
    public static final String MAJOR_GC_OPERATION_NAME = "major_free";

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

    private Constants() {
    }

}
