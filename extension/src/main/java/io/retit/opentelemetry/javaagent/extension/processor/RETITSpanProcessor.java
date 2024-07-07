package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.common.AttributeKey;
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

public class RETITSpanProcessor implements SpanProcessor {

    private final BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder;
    private BatchSpanProcessor delegateBatchSpanProcessor;

    public RETITSpanProcessor(BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder) {
        this.delegateBatchSpanProcessorBuilder = delegateBatchSpanProcessorBuilder;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan readWriteSpan) {
        System.out.println("onStart called");
        boolean logCPUDemand = TelemetryUtils.isLogCpuDemandDefaultTrue();
        boolean logHeapDemand = TelemetryUtils.isLogHeapDemandDefaultTrue();
        boolean logGCEvent = TelemetryUtils.isLogGCEventDefaultTrue();
        boolean logResponseTime = InstanceConfiguration.isLogResponseTime();
        boolean logDiskDemand = InstanceConfiguration.isLogDiskDemand();
        boolean logNetworkDemand = InstanceConfiguration.isLogNetworkDemand();
        TelemetryUtils.addStartResourceDemandValuesToSpanAttributes(
                logCPUDemand,
                logResponseTime,
                logHeapDemand,
                logDiskDemand,
                logCPUDemand || logResponseTime || logHeapDemand || logDiskDemand || logGCEvent || logNetworkDemand,
                readWriteSpan);
        if (readWriteSpan.toSpanData() != null) {
            System.out.println("Start attributes: " + readWriteSpan.toSpanData().getAttributes());
        } else {
            System.out.println("SpanData is null");
        }
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
        System.out.println("onStart called");
        final SpanData currentReadableSpanData = readableSpan.toSpanData();
        final Attributes attributes = currentReadableSpanData.getAttributes();
        final AttributesBuilder attributesBuilder = Attributes.builder().putAll(attributes);
        boolean logCPUDemand = TelemetryUtils.isLogCpuDemandDefaultTrue();
        boolean logTotalCPUTimeUsed = TelemetryUtils.isLogTotalCpuTimeUsedDefaultTrue();
        boolean logHeapDemand = TelemetryUtils.isLogHeapDemandDefaultTrue();
        boolean logTotalHeapDemand = TelemetryUtils.isLogTotalHeapDemandDefaultTrue();
        boolean logGCEvent = TelemetryUtils.isLogGCEventDefaultTrue();
        boolean logResponseTime = InstanceConfiguration.isLogResponseTime();
        boolean logDiskDemand = InstanceConfiguration.isLogDiskDemand();
        boolean logTotalDiskReadDemand = InstanceConfiguration.isLogTotalDiskReadDemand();
        boolean logTotalDiskWriteDemand = InstanceConfiguration.isLogTotalDiskWriteDemand();
        boolean logTotalDiskDemand = InstanceConfiguration.isLogTotalDiskDemand();
        boolean logNetworkDemand = InstanceConfiguration.isLogNetworkDemand();
        final Attributes mergedAttributes =
            TelemetryUtils.addEndResourceDemandValuesToSpanAttributes(
                attributesBuilder,
                logCPUDemand,
                logResponseTime,
                logHeapDemand,
                logDiskDemand,
                logCPUDemand || logResponseTime || logHeapDemand || logDiskDemand || logGCEvent || logNetworkDemand,
                readableSpan);

        Long startCpuTime = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_CPU_TIME));
        Long endCpuTime = mergedAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_CPU_TIME));
        long totalCPUTimeUsed = startCpuTime != null && endCpuTime != null ? endCpuTime - startCpuTime : 0;

        Long startHeapByteAllocation = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION));
        Long endHeapByteAllocation = mergedAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION));
        long totalHeapDemand = startHeapByteAllocation != null && endHeapByteAllocation != null ? endHeapByteAllocation - startHeapByteAllocation : 0;

        Long startDiskReadDemand = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_READ_DEMAND));
        Long endDiskReadDemand = mergedAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND));
        long totalDiskReadDemand = startDiskReadDemand != null && endDiskReadDemand != null ? endDiskReadDemand - startDiskReadDemand : 0;

        Long startDiskWriteDemand = attributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND));
        Long endDiskWriteDemand = mergedAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND));
        long totalDiskWriteDemand = startDiskWriteDemand != null && endDiskWriteDemand != null ? endDiskWriteDemand - startDiskWriteDemand : 0;

        long totalDiskDemand = totalDiskReadDemand + totalDiskWriteDemand;

        Attributes finalAttributes = TelemetryUtils.addResourceDemandMetricsToSpanAttributes(attributesBuilder, logTotalCPUTimeUsed, totalCPUTimeUsed,
                logTotalDiskReadDemand, totalDiskReadDemand, logTotalDiskWriteDemand, totalDiskWriteDemand, logTotalDiskDemand,
                totalDiskDemand, logTotalHeapDemand, totalHeapDemand,  readableSpan);

        return TelemetryUtils.createReadableSpan(readableSpan, finalAttributes);
    }

    @Override
    public boolean isEndRequired() {
        System.out.println("isEndRequired called");
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

    public BatchSpanProcessorBuilder getDelegateBatchSpanProcessorBuilder() {
        return delegateBatchSpanProcessorBuilder;
    }

    public void buildBatchSpanProcessor() {
        delegateBatchSpanProcessor = delegateBatchSpanProcessorBuilder.build();
    }

    // visible for testing
    protected BatchSpanProcessor getDelegateBatchSpanProcessor() {
        return delegateBatchSpanProcessor;
    }
}
