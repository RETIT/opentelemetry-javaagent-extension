package io.retit.opentelemetry.javaagent.extension.commons;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.retit.opentelemetry.javaagent.extension.resources.CommonResourceDemandDataCollector;
import io.retit.opentelemetry.javaagent.extension.resources.IResourceDemandDataCollector;

/**
 * Utility class to add resource demand data to OpenTelemetry span.
 */
public class TelemetryUtils {
    private static final AttributeKey<String> DB_SYSTEM = AttributeKey.stringKey("db.system");

    private static final IResourceDemandDataCollector RESOURCE_DEMAND_DATA_COLLECTOR =
            CommonResourceDemandDataCollector.getResourceDemandDataCollector();

    private TelemetryUtils() {
    }

    /**
     * This method adds the resource demand values at span start to the span.
     *
     * @param logCPUTime         - configuration if CPU demand should be added to the span
     * @param logSystemTime      - configuration if system should be added to the span
     * @param logHeapConsumption - configuration if heap should be added to the span
     * @param logDiskDemand      - configuration if disk demand should be added to the span
     * @param logNetworkDemand   - configuration if network demand should be added to the span
     * @param readWriteSpan      - the span to which the attributes should be added
     */
    public static void addStartResourceDemandValuesToSpanAttributes(final boolean logCPUTime, final boolean logSystemTime, final boolean logHeapConsumption,
                                                                    final boolean logDiskDemand, final boolean logNetworkDemand, final ReadWriteSpan readWriteSpan) {
        if (logSystemTime) {
            readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_SYSTEM_TIME, System.nanoTime());
        }
        if (!isExternalDatabaseCall(readWriteSpan)) {
            boolean threadNameRequired = false;

            if (logHeapConsumption) {
                long heapDemand = RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadAllocatedBytes();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION,
                        heapDemand);
                threadNameRequired = true;
            }
            if (logDiskDemand) {
                long[] readAndWriteBytes = RESOURCE_DEMAND_DATA_COLLECTOR.getDiskBytesReadAndWritten();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_DISK_READ_DEMAND, readAndWriteBytes[0]);
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND, readAndWriteBytes[1]);
                threadNameRequired = true;
            }
            if (logNetworkDemand) {
                long[] readAndWriteBytes = RESOURCE_DEMAND_DATA_COLLECTOR.getNetworkBytesReadAndWritten();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_NETWORK_READ_DEMAND, readAndWriteBytes[0]);
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_NETWORK_WRITE_DEMAND, readAndWriteBytes[1]);
                threadNameRequired = true;
            }
            if (logCPUTime) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_CPU_TIME, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadCpuTime());
                threadNameRequired = true;
            }

            if (threadNameRequired) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_SPAN_START_THREAD, Thread.currentThread().getId());
            }
        }
    }

    /**
     * This method adds the resource demand values at span end to the span.
     *
     * @param logCPUTime         - configuration if CPU demand should be added to the span
     * @param logSystemTime      - configuration if system should be added to the span
     * @param logHeapConsumption - configuration if heap should be added to the span
     * @param logDiskDemand      - configuration if disk demand should be added to the span
     * @param logNetworkDemand   - configuration if network demand should be added to the span
     * @param readWriteSpan      - the span to which the attributes should be added
     */
    public static void addEndResourceDemandValuesToSpanAttributes(final boolean logCPUTime,
                                                                  final boolean logSystemTime, final boolean logHeapConsumption, final boolean logDiskDemand,
                                                                  final boolean logNetworkDemand, final ReadWriteSpan readWriteSpan) {
        readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_LOG_SYSTEM_TIME, System.currentTimeMillis());
        if (logSystemTime) {
            readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_SYSTEM_TIME, System.nanoTime());
        }
        if (!isExternalDatabaseCall(readWriteSpan)) {
            boolean threadNameRequired = false;
            if (logCPUTime) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_CPU_TIME, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadCpuTime());
                threadNameRequired = true;
            }
            if (logDiskDemand) {
                long[] readAndWriteBytes = RESOURCE_DEMAND_DATA_COLLECTOR.getDiskBytesReadAndWritten();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND, readAndWriteBytes[0]);
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND, readAndWriteBytes[1]);
                threadNameRequired = true;
            }
            if (logNetworkDemand) {
                long[] readAndWriteBytes = RESOURCE_DEMAND_DATA_COLLECTOR.getNetworkBytesReadAndWritten();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_NETWORK_READ_DEMAND, readAndWriteBytes[0]);
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_NETWORK_WRITE_DEMAND, readAndWriteBytes[1]);
                threadNameRequired = true;
            }
            if (logHeapConsumption) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadAllocatedBytes());
                threadNameRequired = true;
            }
            if (threadNameRequired) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_SPAN_END_THREAD, Thread.currentThread().getId());
            }
        }
    }

    /**
     * Determines if the current span represents a database call.
     *
     * @param span - the span that needs to be checked.
     * @return - true if the span represents a database call, false otherwise
     */
    public static boolean isExternalDatabaseCall(final ReadableSpan span) {
        return span.getAttribute(DB_SYSTEM) != null;
    }

}

