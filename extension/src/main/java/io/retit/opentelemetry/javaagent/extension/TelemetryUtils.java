package io.retit.opentelemetry.javaagent.extension;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.retit.opentelemetry.javaagent.extension.resources.CommonResourceDemandDataCollector;
import io.retit.opentelemetry.javaagent.extension.resources.IResourceDemandDataCollector;

import java.util.List;

public class TelemetryUtils {
    private static final AttributeKey<String> DB_SYSTEM = AttributeKey.stringKey("db.system");

    private static final IResourceDemandDataCollector RESOURCE_DEMAND_DATA_COLLECTOR =
            CommonResourceDemandDataCollector.getResourceDemandDataCollector();

    private TelemetryUtils() {
    }

    public static boolean isLogCpuDemandDefaultTrue() {
        return InstanceConfiguration.getBooleanProperty(Constants.RETIT_APM_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
    }

    public static boolean isLogTotalCpuTimeUsedDefaultTrue() {
        return InstanceConfiguration.getBooleanProperty(Constants.RETIT_APM_TOTAL_CPU_TIMES_USED_LOGGING_CONFIGURATION_PROPERTY, true);
    }

    public static boolean isLogHeapDemandDefaultTrue() {
        return InstanceConfiguration.getBooleanProperty(Constants.RETIT_APM_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
    }

    public static boolean isLogTotalHeapDemandDefaultTrue() {
        return InstanceConfiguration.getBooleanProperty(Constants.RETIT_APM_TOTAL_HEAP_DEMAND_CONFIGURATION_PROPERTY, true);
    }

    public static boolean isLogGCEventDefaultTrue() {
        return InstanceConfiguration.getBooleanProperty(Constants.RETIT_APM_GC_EVENT_LOGGING_CONFIGURATION_PROPERTY, true);
    }

    public static SpanData createSpanData(SpanData spanData, Attributes attributes) {
        return new SpanData() {
            @Override
            public String getName() {
                return spanData.getName();
            }

            @Override
            public SpanKind getKind() {
                return spanData.getKind();
            }

            @Override
            public SpanContext getSpanContext() {
                return spanData.getSpanContext();
            }

            @Override
            public SpanContext getParentSpanContext() {
                return spanData.getParentSpanContext();
            }

            @Override
            public StatusData getStatus() {
                return spanData.getStatus();
            }

            @Override
            public long getStartEpochNanos() {
                return spanData.getStartEpochNanos();
            }

            @Override
            public Attributes getAttributes() {
                return attributes;
            }

            @Override
            public List<EventData> getEvents() {
                return spanData.getEvents();
            }

            @Override
            public List<LinkData> getLinks() {
                return spanData.getLinks();
            }

            @Override
            public long getEndEpochNanos() {
                return spanData.getEndEpochNanos();
            }

            @Override
            public boolean hasEnded() {
                return spanData.hasEnded();
            }

            @Override
            public int getTotalRecordedEvents() {
                return spanData.getTotalRecordedEvents();
            }

            @Override
            public int getTotalRecordedLinks() {
                return spanData.getTotalRecordedLinks();
            }

            @Override
            public int getTotalAttributeCount() {
                return spanData.getTotalAttributeCount();
            }

            @Override
            public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
                return spanData.getInstrumentationLibraryInfo();
            }

            @Override
            public InstrumentationScopeInfo getInstrumentationScopeInfo() {
                return spanData.getInstrumentationScopeInfo();
            }

            @Override
            public Resource getResource() {
                return spanData.getResource();
            }
        };
    }

    public static ReadableSpan createReadableSpan(ReadableSpan readableSpan, Attributes mergedAttributes) {
        return new ReadableSpan() {
            @Override
            public SpanContext getSpanContext() {
                return readableSpan.getSpanContext();
            }

            @Override
            public SpanContext getParentSpanContext() {
                return readableSpan.getParentSpanContext();
            }

            @Override
            public String getName() {
                return readableSpan.getName();
            }

            @Override
            public SpanData toSpanData() {
                return TelemetryUtils.createSpanData(readableSpan.toSpanData(), mergedAttributes);
            }

            @Override
            public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
                return readableSpan.getInstrumentationLibraryInfo();
            }

            @Override
            public InstrumentationScopeInfo getInstrumentationScopeInfo() {
                return readableSpan.getInstrumentationScopeInfo();
            }

            @Override
            public boolean hasEnded() {
                return readableSpan.hasEnded();
            }

            @Override
            public long getLatencyNanos() {
                return readableSpan.getLatencyNanos();
            }

            @Override
            public SpanKind getKind() {
                return readableSpan.getKind();
            }

            @Override
            public <T> T getAttribute(AttributeKey<T> key) {
                return toSpanData().getAttributes().get(key);
            }
        };
    }

    public static void addCostumDataToSpanAttributes(double value, ReadWriteSpan readWriteSpan) {
        readWriteSpan.setAttribute("randomNumberOnStart", value);
    }

    public static void addStartResourceDemandValuesToSpanAttributes(boolean logCPUTime, boolean logSystemTime, boolean logHeapConsumption,
                                                                    boolean logDiskDemand, boolean logThreadName, ReadWriteSpan readWriteSpan) {
        if (logSystemTime) {
            readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_SYSTEM_TIME, System.nanoTime());
        }
        if (!isExternalDatabaseCall(readWriteSpan)) {
            if (logThreadName) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_SPAN_START_THREAD, Thread.currentThread().getId());
            }
            if (logHeapConsumption) {
                readWriteSpan.setAttribute(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION,
                        RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadAllocatedBytes());
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

    public static Attributes addEndResourceDemandValuesToSpanAttributes(AttributesBuilder attributesBuilder, boolean logCPUTime,
                                                                        boolean logSystemTime, boolean logHeapConsumption, boolean logDiskDemand,
                                                                        boolean logThreadName, ReadableSpan readableSpan) {
        attributesBuilder.put(Constants.SPAN_ATTRIBUTE_LOG_SYSTEM_TIME, System.currentTimeMillis());
        if (logSystemTime) {
            attributesBuilder.put(Constants.SPAN_ATTRIBUTE_END_SYSTEM_TIME, System.nanoTime());
        }
        if (!isExternalDatabaseCall(readableSpan)) {
            if (logThreadName) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_SPAN_END_THREAD, Thread.currentThread().getId());
            }
            if (logCPUTime) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_END_CPU_TIME, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadCpuTime());
            }
            if (logDiskDemand) {
                long[] readAndWriteBytes = RESOURCE_DEMAND_DATA_COLLECTOR.getDiskBytesReadAndWritten();
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND, readAndWriteBytes[0]);
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND, readAndWriteBytes[1]);
            }
            if (logHeapConsumption) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION,
                        100000000);
                //attributesBuilder.put(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION,
                  //      RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadAllocatedBytes());
            }
        }
        return attributesBuilder.build();
    }

    public static Attributes addResourceDemandMetricsToSpanAttributes(AttributesBuilder attributesBuilder,
                                                                      boolean logTotalCPUTimeUsed, long totalCPUTimeUsed,
                                                                      boolean logTotalDiskReadDemand, long totalDiskReadDemand,
                                                                      boolean logTotalDiskWriteDemand, long totalDiskWriteDemand,
                                                                      boolean logTotalHeapDemand, long totalHeapDemand,
                                                                      boolean logTotalStorageDemand, long totalStorageDemand,
                                                                      ReadableSpan readableSpan) {
        if (!isExternalDatabaseCall(readableSpan)) {
            if (logTotalCPUTimeUsed) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_TOTAL_CPU_TIME_USED, totalCPUTimeUsed);
                System.out.println("Total CPU Time Logged: " + totalCPUTimeUsed);
            }
            if (logTotalDiskReadDemand) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_TOTAL_DISK_READ_DEMAND, totalDiskReadDemand);
                System.out.println("Total Disk Read Demand Logged: " + totalDiskReadDemand);
            }
            if (logTotalDiskWriteDemand) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_TOTAL_DISK_WRITE_DEMAND, totalDiskWriteDemand);
                System.out.println("Total Disk Write Demand Logged: " + totalDiskWriteDemand);
            }

            if (logTotalStorageDemand) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_TOTAL_STORAGE_DEMAND, totalStorageDemand);
                System.out.println("Total Storage Demand Logged: " + totalStorageDemand);
            }

            if (logTotalHeapDemand) {
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_TOTAL_HEAP_DEMAND, totalHeapDemand);
                System.out.println("Total Heap Demand Logged: " + totalHeapDemand);
            }

            return attributesBuilder.build();
        }
        return attributesBuilder.build();
    }

    private static boolean isExternalDatabaseCall(ReadableSpan span) {
        return span.getAttribute(DB_SYSTEM) != null;
    }
}

