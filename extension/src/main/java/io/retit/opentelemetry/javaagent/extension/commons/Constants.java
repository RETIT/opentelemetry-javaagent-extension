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

package io.retit.opentelemetry.javaagent.extension.commons;

/**
 * Constants class for configuration values.
 */
@SuppressWarnings("checkstyle:javadocVariable")
public class Constants {
    public static final String NETWORK_NAMESPACE = "network";
    public static final String THREAD_NAMESPACE = "thread";
    public static final String USER_NAMESPACE = "user";
    public static final String CLIENT_NAMESPACE = "client";
    public static final String INSTANCE_NAMESPACE = "instance";

    public static final String RETIT_NAMESPACE = "io.retit";
    public static final String RETIT_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".log.response.time";
    public static final String RETIT_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".log.cpu.demand";
    public static final String RETIT_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".log.heap.demand";
    public static final String RETIT_NETWORK_DEMAND_LOGGING_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".log.network.demand";
    public static final String RETIT_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".log.disk.demand";
    public static final String RETIT_GC_EVENT_LOGGING_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".log.gc.event";
    public static final String RETIT_THREAD_NAME_LOGGING_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".log.thread.name";

    public static final String RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".emissions.cloud.provider";
    public static final String RETIT_EMISSIONS_CLOUD_PROVIDER_REGION_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".emissions.cloud.provider.region";
    public static final String RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".emissions.cloud.provider.instance.type";
    public static final String RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".emissions.storage.type";
    public static final String RETIT_EMISSIONS_MICROARCHITECTURE_CONFIGURATION_PROPERTY = RETIT_NAMESPACE + ".microarchitecture";

    public static final String RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AWS = "AWS";
    public static final String RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_GCP = "GCP";
    public static final String RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY_VALUE_AZURE = "Azure";

    public static final String RETIT_EMISSIONS_STORAGE_TYPE_CONFIGURATION_PROPERTY_VALUE_SSD = "SSD";
    public static final String RETIT_VALUE_NOT_SET = "not-set";

    public static final String STARTCPUTIME_FIELD = "startcputime";
    public static final String ENDCPUTIME_FIELD = "endcputime";

    public static final String STARTSYSTEMTIME_FIELD = "startsystemtime";
    public static final String ENDSYSTEMTIME_FIELD = "endsystemtime";

    public static final String STARTHEAPBYTEALLOCATION_FIELD = "startheapbyteallocation";
    public static final String ENDHEAPBYTEALLOCATION_FIELD = "endheapbyteallocation";

    public static final String STARTDISKREADDEMAND_FIELD = "startdiskreaddemand";
    public static final String ENDDISKREADDEMAND_FIELD = "enddiskreaddemand";
    public static final String STARTNETWORKREADDEMAND_FIELD = "startnetworkreaddemand";
    public static final String ENDNETWORKREADDEMAND_FIELD = "endnetworkreaddemand";

    public static final String STARTDISKWRITEDEMAND_FIELD = "startdiskwritedemand";
    public static final String ENDDISKWRITEDEMAND_FIELD = "enddiskwritedemand";
    public static final String STARTNETWORKWRITEDEMAND_FIELD = "startnetworkwritedemand";
    public static final String ENDNETWORKWRITEDEMAND_FIELD = "endnetworkwritedemand";
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
    public static final String SPAN_ATTRIBUTE_START_NETWORK_READ_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTNETWORKREADDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_START_NETWORK_WRITE_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTNETWORKWRITEDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_START_CPU_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, STARTCPUTIME_FIELD);
    public static final String SPAN_ATTRIBUTE_END_CPU_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDCPUTIME_FIELD);
    public static final String SPAN_ATTRIBUTE_END_DISK_READ_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDDISKREADDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDDISKWRITEDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_END_NETWORK_READ_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDNETWORKREADDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_END_NETWORK_WRITE_DEMAND = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDNETWORKWRITEDEMAND_FIELD);
    public static final String SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDHEAPBYTEALLOCATION_FIELD);
    public static final String SPAN_ATTRIBUTE_END_SYSTEM_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, ENDSYSTEMTIME_FIELD);

    public static final String SPAN_ATTRIBUTE_LOG_SYSTEM_TIME = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, LOG_SYSTEM_TIME_FIELD);
    public static final String SPAN_ATTRIBUTE_TOTAL_HEAP_SIZE = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, TOTAL_HEAP_SIZE_FIELD);
    public static final String SPAN_ATTRIBUTE_SPAN_START_THREAD = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, START_THREAD_FIELD);
    public static final String SPAN_ATTRIBUTE_SPAN_END_THREAD = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, END_THREAD_FIELD);
    public static final String JAVA_AGENT_INSTRUMENTATION_NAME_GC_LISTENER = String.format(STRING_DECIMAL_FORMAT, RETIT_NAMESPACE, GC_LISTENER_NAME);

    public static final String RETIT_EMISSION_NAMESPACE = RETIT_NAMESPACE + ".emissions";
    public static final String PROCESS_CPU_TIME_FIELD = ".java.process.cpu.time";
    public static final String SPAN_ATTRIBUTE_PROCESS_CPU_TIME = RETIT_EMISSION_NAMESPACE + PROCESS_CPU_TIME_FIELD;

    public static final String GC_PREFIX = "gc";
    public static final String JAVA_AGENT_GC_OPERATION_NAME_MINOR_FREE =
            String.format("%s.%s.%s", RETIT_NAMESPACE, GC_PREFIX, MINOR_GC_OPERATION_NAME);
    public static final String JAVA_AGENT_GC_OPERATION_NAME_MAJOR_FREE =
            String.format("%s.%s.%s", RETIT_NAMESPACE, GC_PREFIX, MAJOR_GC_OPERATION_NAME);

    private Constants() {
    }

}
