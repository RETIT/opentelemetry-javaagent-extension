package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanContext;
import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.TelemetryUtils;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.retit.opentelemetry.javaagent.extension.commons.NativeFacade;
import io.retit.opentelemetry.javaagent.extension.resources.CommonResourceDemandDataCollector;
import io.retit.opentelemetry.javaagent.extension.resources.IResourceDemandDataCollector;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class RETITSpanProcessor implements SpanProcessor {

    @Getter
    private final BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder;
    private BatchSpanProcessor delegateBatchSpanProcessor;

    public RETITSpanProcessor(BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder) {
        this.delegateBatchSpanProcessorBuilder = delegateBatchSpanProcessorBuilder;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan readWriteSpan) {
        boolean logCPUDemand = TelemetryUtils.isLogCpuDemandDefaultTrue();
        boolean logHeapDemand = TelemetryUtils.isLogHeapDemandDefaultTrue();
        boolean logGCEvent = TelemetryUtils.isLogGCEventDefaultTrue();
        boolean logResponseTime = InstanceConfiguration.isLogResponseTime();
        boolean logDiskDemand = InstanceConfiguration.isLogDiskDemand();
        boolean logNetworkDemand = InstanceConfiguration.isLogNetworkDemand();

        long startJavaThreadId = Thread.currentThread().getId();

        TelemetryUtils.addStartResourceDemandValuesToSpanAttributes(
                logCPUDemand,
                logResponseTime,
                logHeapDemand,
                logDiskDemand,
                logCPUDemand || logResponseTime || logHeapDemand || logDiskDemand || logGCEvent || logNetworkDemand,
                readWriteSpan);
        readWriteSpan.setAttribute("startJavaThreadId", startJavaThreadId);
    }

    @Override
    public boolean isStartRequired() {
        System.out.println("isStartRequired called");
        return true;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
        delegateBatchSpanProcessor.onEnd(beforeEnd(readableSpan));
    }

    /**
     * Custom before end hook.
     * <p>
     * Converts the span into a writable format before adding our resource demand attributes
     * into the existing list of attributes.
     *
     * @param readableSpan - {@link ReadableSpan} from the {@code onEnd()} hook
     * @return {@link ReadableSpan} containing preexisting and our custom attributes
     */
    private ReadableSpan beforeEnd(ReadableSpan readableSpan) {
        final SpanData currentReadableSpanData = readableSpan.toSpanData();
        final Attributes attributes = currentReadableSpanData.getAttributes();
        final AttributesBuilder attributesBuilder = Attributes.builder().putAll(attributes);
        boolean logCPUDemand = TelemetryUtils.isLogCpuDemandDefaultTrue();
        boolean logHeapDemand = TelemetryUtils.isLogHeapDemandDefaultTrue();
        boolean logGCEvent = TelemetryUtils.isLogGCEventDefaultTrue();
        boolean logResponseTime = InstanceConfiguration.isLogResponseTime();
        boolean logDiskDemand = InstanceConfiguration.isLogDiskDemand();
        boolean logNetworkDemand = InstanceConfiguration.isLogNetworkDemand();

        Long startJavaThreadIdObj = readableSpan.getAttribute(AttributeKey.longKey("startJavaThreadId"));
        long endJavaThreadId = Thread.currentThread().getId();

        long totalHeapDemand = 0;
        long totalCpuTimeUsed = 0;
        long totalStorageDemand = 0;

        if (startJavaThreadIdObj != null && startJavaThreadIdObj == endJavaThreadId) {
            System.out.println("same thread ID");
            Long startCpuTime = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_CPU_TIME));
            Long endCpuTime = readableSpan.getAttribute(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_CPU_TIME));
            totalCpuTimeUsed = startCpuTime != null && endCpuTime != null ? endCpuTime - startCpuTime : 0;

            Long startHeapByteAllocation = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION));
            Long endHeapByteAllocation = readableSpan.getAttribute((AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION)));
            totalHeapDemand = startHeapByteAllocation != null && endHeapByteAllocation != null ? endHeapByteAllocation - startHeapByteAllocation : 0;

            Long startDiskReadDemand = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_READ_DEMAND));
            Long endDiskReadDemand = readableSpan.getAttribute((AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND)));
            long totalDiskReadDemand = startDiskReadDemand != null && endDiskReadDemand != null ? endDiskReadDemand - startDiskReadDemand : 0;

            Long startDiskWriteDemand = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND));
            Long endDiskWriteDemand = readableSpan.getAttribute((AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND)));
            long totalDiskWriteDemand = startDiskWriteDemand != null && endDiskWriteDemand != null ? (endDiskWriteDemand - startDiskWriteDemand) : 0;

            totalStorageDemand = totalDiskReadDemand + totalDiskWriteDemand;

            if (!readableSpan.getParentSpanContext().isValid()) {
                System.out.println("Top level span");
                MetricPublishingService.getInstance().publishEmissions(Attributes.of(AttributeKey.stringKey("Servicecall"), readableSpan.getName()), totalStorageDemand, totalCpuTimeUsed, totalHeapDemand);
            } else {
                System.out.println("No top level span");
            }
        } else {
            System.out.println("Different thread ID");
        }

        final Attributes mergedAttributes =
                TelemetryUtils.addEndResourceDemandValuesToSpanAttributes(
                        attributesBuilder,
                        logCPUDemand,
                        logResponseTime,
                        logHeapDemand,
                        logDiskDemand,
                        logCPUDemand || logResponseTime || logHeapDemand || logDiskDemand || logGCEvent || logNetworkDemand,
                        readableSpan);

        return TelemetryUtils.createReadableSpan(readableSpan, mergedAttributes);
    }

    @Override
    public boolean isEndRequired() {
        return delegateBatchSpanProcessor.isEndRequired();
    }

    @Override
    public CompletableResultCode shutdown() {
        return delegateBatchSpanProcessor.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return delegateBatchSpanProcessor.forceFlush();
    }

    @Override
    public void close() {
        delegateBatchSpanProcessor.close();
    }

    public void buildBatchSpanProcessor() {
        delegateBatchSpanProcessor = delegateBatchSpanProcessorBuilder.build();
    }

    // visible for testing
    protected BatchSpanProcessor getDelegateBatchSpanProcessor() {
        return delegateBatchSpanProcessor;
    }
}
