package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.common.AttributeKey;
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
import lombok.Getter;

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

        TelemetryUtils.addStartResourceDemandValuesToSpanAttributes(
                logCPUDemand,
                logResponseTime,
                logHeapDemand,
                logDiskDemand,
                logCPUDemand || logResponseTime || logHeapDemand || logDiskDemand || logGCEvent || logNetworkDemand,
                readWriteSpan);
    }

    @Override
    public boolean isStartRequired() {
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

        final Attributes mergedAttributes =
                TelemetryUtils.addEndResourceDemandValuesToSpanAttributes(
                        attributesBuilder,
                        logCPUDemand,
                        logResponseTime,
                        logHeapDemand,
                        logDiskDemand,
                        logCPUDemand || logResponseTime || logHeapDemand || logDiskDemand || logGCEvent || logNetworkDemand,
                        readableSpan);

        Attributes finalAttributes = TelemetryUtils.addEmissionAndUsageDataToSpanAttributes(logCPUDemand,
                logHeapDemand, logDiskDemand, logNetworkDemand, attributesBuilder, mergedAttributes, readableSpan);

        if (readableSpan.getParentSpanContext() != null && !readableSpan.getParentSpanContext().isValid()) {
            attributesBuilder.put(AttributeKey.stringKey("Servicecall"), readableSpan.getName());
            finalAttributes = attributesBuilder.build();
            Double cpuEmissions = finalAttributes.get(AttributeKey.doubleKey("cpuEmissionsInMg"));
            Double memoryEmissions = finalAttributes.get(AttributeKey.doubleKey("memoryEmissionsInMg"));
            Double storageEmissions = finalAttributes.get(AttributeKey.doubleKey("storageEmissionsInMg"));
            Double embodiedEmissions = finalAttributes.get(AttributeKey.doubleKey("embodiedEmissionsInMg"));

            MetricPublishingService.getInstance().publishEmissions(cpuEmissions, memoryEmissions, storageEmissions,
                    embodiedEmissions, Attributes.of(AttributeKey.stringKey("Servicecall"), readableSpan.getName()));
        }
        return TelemetryUtils.createReadableSpan(readableSpan, finalAttributes);
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
