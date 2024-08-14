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
import io.retit.opentelemetry.javaagent.extension.emissions.cpu.CpuEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.cpu.EmbodiedEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.memory.MemoryEmissions;
import io.retit.opentelemetry.javaagent.extension.emissions.storage.StorageEmissions;
import io.retit.opentelemetry.javaagent.extension.resources.CommonResourceDemandDataCollector;
import io.retit.opentelemetry.javaagent.extension.resources.IResourceDemandDataCollector;

import java.util.List;

public class TelemetryUtils {
    private static final AttributeKey<String> DB_SYSTEM = AttributeKey.stringKey("db.system");

    private static final IResourceDemandDataCollector RESOURCE_DEMAND_DATA_COLLECTOR =
            CommonResourceDemandDataCollector.getResourceDemandDataCollector();

    private TelemetryUtils() {
    }

    public static SpanData createSpanData(final SpanData spanData, final Attributes attributes) {
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

    public static ReadableSpan createReadableSpan(final ReadableSpan readableSpan, final Attributes mergedAttributes) {
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
                return createSpanData(readableSpan.toSpanData(), mergedAttributes);
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
            public <T> T getAttribute(final AttributeKey<T> key) {
                return toSpanData().getAttributes().get(key);
            }
        };
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

    public static Attributes addEndResourceDemandValuesToSpanAttributes(final AttributesBuilder attributesBuilder, final boolean logCPUTime,
                                                                        final boolean logSystemTime, final boolean logHeapConsumption, final boolean logDiskDemand,
                                                                        final boolean logThreadName, final ReadableSpan readableSpan) {
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
                attributesBuilder.put(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION, RESOURCE_DEMAND_DATA_COLLECTOR.getCurrentThreadAllocatedBytes());
            }
        }
        return attributesBuilder.build();
    }

    /**
     * Adds emission and usage data to the span attributes.
     * <p>
     * This method calculates and adds various energy usage and emission metrics to the span attributes
     * based on the provided flags and span data. It includes CPU, memory, and storage energy usage and emissions.
     *
     * @param logCPUDemand      Flag indicating whether to log CPU demand.
     * @param logHeapDemand     Flag indicating whether to log heap demand.
     * @param logDiskDemand     Flag indicating whether to log disk demand.
     * @param logNetworkDemand  Flag indicating whether to log network demand.
     * @param attributesBuilder The builder containing the attributes of the current ReadableSpan to put values.
     * @param attributesOfSpan  The existing attributes of the span.
     * @param readableSpan      The span from which to read additional data.
     * @return The updated attributes including the emission and usage data.
     */
    public static Attributes addEmissionAndUsageDataToSpanAttributes(final boolean logCPUDemand, final boolean logHeapDemand, final boolean logDiskDemand, final boolean logNetworkDemand,
                                                                     final AttributesBuilder attributesBuilder, final Attributes attributesOfSpan, final ReadableSpan readableSpan) {
        if (!isExternalDatabaseCall(readableSpan)) {
            Long startThread = readableSpan.getAttribute(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_SPAN_START_THREAD));
            Long endThread = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_SPAN_END_THREAD));

            if (startThread != null && startThread.equals(endThread)) {

                addCpuEmissionDataToSpanAttributes(logCPUDemand, attributesBuilder, attributesOfSpan);

                addHeapEmissionDataToSpanAttributes(logHeapDemand, attributesBuilder, attributesOfSpan);

                addDiskEmissionDataToSpanAttributes(logDiskDemand, attributesBuilder, attributesOfSpan);
            }
        }
        return attributesBuilder.build();
    }

    // TODO - adjust implementation this calculation can be done externally
    private static void addDiskEmissionDataToSpanAttributes(final boolean logDiskDemand, final AttributesBuilder attributesBuilder, final Attributes attributesOfSpan) {
        if (logDiskDemand) {
            Long startDiskReadDemand = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_READ_DEMAND));
            Long endDiskReadDemand = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND));
            long totalDiskReadDemand = startDiskReadDemand != null && endDiskReadDemand != null ? endDiskReadDemand - startDiskReadDemand : 0;

            Long startDiskWriteDemand = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND));
            Long endDiskWriteDemand = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND));
            long totalDiskWriteDemand = startDiskWriteDemand != null && endDiskWriteDemand != null ? (endDiskWriteDemand - startDiskWriteDemand) : 0;

            long totalStorageDemand = totalDiskReadDemand + totalDiskWriteDemand;

            double storageEnergyConsumption = StorageEmissions.getInstance().energyUsageInKiloWattHours(totalStorageDemand);
            attributesBuilder.put(AttributeKey.doubleKey("storageWattHoursUsage"), storageEnergyConsumption * 1000);
            attributesBuilder.put(AttributeKey.doubleKey("storageEmissionsInMg"), StorageEmissions.getInstance().calculateStorageEmissionsInMilliGram(storageEnergyConsumption));
        }
    }

    // TODO - adjust implementation this calculation can be done externally
    private static void addHeapEmissionDataToSpanAttributes(final boolean logHeapDemand, final AttributesBuilder attributesBuilder, final Attributes attributesOfSpan) {
        if (logHeapDemand) {
            Long startHeapByteAllocation = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION));
            Long endHeapByteAllocation = attributesOfSpan.get((AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION)));
            long totalHeapDemand = startHeapByteAllocation != null && endHeapByteAllocation != null ? endHeapByteAllocation - startHeapByteAllocation : 0;
            if (totalHeapDemand > 0) {
                double memoryEnergyConsumption = MemoryEmissions.getInstance().energyUsageInKiloWattHours(totalHeapDemand);
                attributesBuilder.put(AttributeKey.doubleKey("memoryEmissionsInMg"),
                        MemoryEmissions.getInstance().calculateMemoryEmissionsInMilliGram(memoryEnergyConsumption));
                attributesBuilder.put(AttributeKey.doubleKey("memoryWattHoursUsage"), memoryEnergyConsumption * 1000);
            } else {
                attributesBuilder.put(AttributeKey.doubleKey("memoryEmissionsInMg"), 0.0);
            }
        }
    }

    // TODO - adjust implementation this calculation can be done externally
    private static void addCpuEmissionDataToSpanAttributes(final boolean logCPUDemand, final AttributesBuilder attributesBuilder, final Attributes attributesOfSpan) {
        if (logCPUDemand) {
            Long startCpuTime = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_CPU_TIME));
            Long endCpuTime = attributesOfSpan.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_CPU_TIME));
            long totalCpuTimeUsed = startCpuTime != null && endCpuTime != null ? endCpuTime - startCpuTime : 0;
            double embodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInMilliGram(totalCpuTimeUsed);
            double cpuKwhUsage = CpuEmissions.getInstance().calculateKwhUsed(totalCpuTimeUsed);
            attributesBuilder.put(AttributeKey.doubleKey("cpuWattHoursUsage"), cpuKwhUsage * 1000);
            attributesBuilder.put(AttributeKey.doubleKey("cpuEmissionsInMg"),
                    CpuEmissions.getInstance().calculateCpuEmissionsInMilliGram(cpuKwhUsage));
            attributesBuilder.put(AttributeKey.doubleKey("embodiedEmissionsInMg"), embodiedEmissions);
        }
    }

    private static boolean isExternalDatabaseCall(final ReadableSpan span) {
        return span.getAttribute(DB_SYSTEM) != null;
    }
}

