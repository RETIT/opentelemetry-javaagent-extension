package io.retit.opentelemetry.javaagent.extension;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.retit.opentelemetry.javaagent.extension.resources.CommonResourceDemandDataCollector;
import io.retit.opentelemetry.javaagent.extension.resources.IResourceDemandDataCollector;

public class TelemetryUtils {
    private static final AttributeKey<String> DB_SYSTEM = AttributeKey.stringKey("db.system");

    private static final IResourceDemandDataCollector RESOURCE_DEMAND_DATA_COLLECTOR =
            CommonResourceDemandDataCollector.getResourceDemandDataCollector();

    private TelemetryUtils() {
    }

    public static void addStartResourceDemandValuesToSpanAttributes(final boolean logCPUTime, final boolean logSystemTime, final boolean logHeapConsumption,
                                                                    final boolean logDiskDemand, final boolean logThreadName, final ReadWriteSpan readWriteSpan) {
        if (logSystemTime) {
            readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_SYSTEM_TIME, System.nanoTime());
        }
        if (!isExternalDatabaseCall(readWriteSpan)) {
            if (logThreadName) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_SPAN_START_THREAD, Thread.currentThread().getId());
            }
            if (logHeapConsumption) {
                long heapDemand = RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadAllocatedBytes();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION,
                        heapDemand);
            }
            if (logDiskDemand) {
                long[] readAndWriteBytes = RESOURCE_DEMAND_DATA_COLLECTOR.getDiskBytesReadAndWritten();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_DISK_READ_DEMAND, readAndWriteBytes[0]);
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND, readAndWriteBytes[1]);
            }
            if (logCPUTime) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_CPU_TIME, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadCpuTime());
            }
        }
    }

    public static void addEndResourceDemandValuesToSpanAttributes(final boolean logCPUTime,
                                                                  final boolean logSystemTime, final boolean logHeapConsumption, final boolean logDiskDemand,
                                                                  final boolean logThreadName, final ReadWriteSpan readWriteSpan) {
        readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_LOG_SYSTEM_TIME, System.currentTimeMillis());
        if (logSystemTime) {
            readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_SYSTEM_TIME, System.nanoTime());
        }
        if (!isExternalDatabaseCall(readWriteSpan)) {
            if (logThreadName) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_SPAN_END_THREAD, Thread.currentThread().getId());
            }
            if (logCPUTime) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_CPU_TIME, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadCpuTime());
            }
            if (logDiskDemand) {
                long[] readAndWriteBytes = RESOURCE_DEMAND_DATA_COLLECTOR.getDiskBytesReadAndWritten();
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND, readAndWriteBytes[0]);
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND, readAndWriteBytes[1]);
            }
            if (logHeapConsumption) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadAllocatedBytes());
            }
        }
    }

    public static boolean isExternalDatabaseCall(final ReadableSpan span) {
        return span.getAttribute(DB_SYSTEM) != null;
    }

}

