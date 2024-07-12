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
import io.retit.opentelemetry.javaagent.extension.config.EnvVariables;

public class RETITSpanProcessor implements SpanProcessor {

   // private final MetricPublishService metricPublishService;
    EnvVariables envVariables = EnvVariables.getEnvInstance();
    private final BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder;
    private BatchSpanProcessor delegateBatchSpanProcessor;

    public RETITSpanProcessor(BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder) {
        this.delegateBatchSpanProcessorBuilder = delegateBatchSpanProcessorBuilder;
        //this.metricPublishService = new MetricPublishService();
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
            System.out.println("Start CPU time: " + readWriteSpan.toSpanData().getAttributes().get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_CPU_TIME)));
        } else {
            System.out.println("Start CPU time  is null");
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
        boolean logTotalStorageDemand = InstanceConfiguration.isLogTotalStorageDemand();
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

        long totalStorageDemand = totalDiskReadDemand + totalDiskWriteDemand;

        Attributes finalAttributes = TelemetryUtils.addResourceDemandMetricsToSpanAttributes(attributesBuilder, logTotalCPUTimeUsed, totalCPUTimeUsed,
                logTotalDiskReadDemand, totalDiskReadDemand, logTotalDiskWriteDemand, totalDiskWriteDemand, logTotalHeapDemand,
                totalHeapDemand, logTotalStorageDemand, totalStorageDemand, readableSpan);

        //System.out.println("Counter ist " + serviceCallCounter++);
        //metricPublishService.publishMetrics(finalAttributes);
        //MetricPublishService.getInstance().incrementServiceCallCounter(Attributes.of(AttributeKey.stringKey("fixed_label"), "fixed_value"));

        MetricPublishService.getInstance().publishStorageEmissions(envVariables, totalStorageDemand, Attributes.of(AttributeKey.stringKey("label_for_storage_demand"), "value"));
        MetricPublishService.getInstance().publishCpuEmissions(envVariables, totalCPUTimeUsed, Attributes.of(AttributeKey.stringKey("label_for_cpu_demand"), "value"));
        MetricPublishService.getInstance().publishEmbeddedEmissions(envVariables, 1, Attributes.of(AttributeKey.stringKey("label_for_embedded_demand"), "value"));
        // metricPublishService.publishCpuEnergy(totalCPUTimeUsed, Attributes.of(AttributeKey.stringKey("cputime"), "cycles"));

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
