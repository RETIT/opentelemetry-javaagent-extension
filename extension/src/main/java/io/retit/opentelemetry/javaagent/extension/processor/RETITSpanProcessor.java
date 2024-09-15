package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.TelemetryUtils;
import io.retit.opentelemetry.javaagent.extension.metrics.MetricPublishingService;

public class RETITSpanProcessor implements ExtendedSpanProcessor {

    private final BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder;
    private BatchSpanProcessor delegateBatchSpanProcessor;

    public RETITSpanProcessor(final BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder) {
        this.delegateBatchSpanProcessorBuilder = delegateBatchSpanProcessorBuilder;
    }

    public BatchSpanProcessorBuilder getDelegateBatchSpanProcessorBuilder() {
        return delegateBatchSpanProcessorBuilder;
    }

    @Override
    public void onStart(final Context parentContext, final ReadWriteSpan readWriteSpan) {
        boolean logCPUDemand = InstanceConfiguration.isLogCpuDemandDefaultTrue();
        boolean logHeapDemand = InstanceConfiguration.isLogHeapDemandDefaultTrue();
        boolean logGCEvent = InstanceConfiguration.isLogGCEventDefaultTrue();
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
    public void onEnd(final ReadableSpan readableSpan) {
        delegateBatchSpanProcessor.onEnd(readableSpan);
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

    @Override
    public void onEnding(ReadWriteSpan readWriteSpan) {
        boolean logCPUDemand = InstanceConfiguration.isLogCpuDemandDefaultTrue();
        boolean logHeapDemand = InstanceConfiguration.isLogHeapDemandDefaultTrue();
        boolean logGCEvent = InstanceConfiguration.isLogGCEventDefaultTrue();
        boolean logResponseTime = InstanceConfiguration.isLogResponseTime();
        boolean logDiskDemand = InstanceConfiguration.isLogDiskDemand();
        boolean logNetworkDemand = InstanceConfiguration.isLogNetworkDemand();

        TelemetryUtils.addEndResourceDemandValuesToSpanAttributes(
                logCPUDemand,
                logResponseTime,
                logHeapDemand,
                logDiskDemand,
                logCPUDemand || logResponseTime || logHeapDemand || logDiskDemand || logGCEvent || logNetworkDemand,
                readWriteSpan);

        if (readWriteSpan.getParentSpanContext() != null && !readWriteSpan.getParentSpanContext().isValid()) {
            // publish resource demand vector for top level transactions as metric for SCI calculations in Grafana
            MetricPublishingService.getInstance().publishResourceDemandVectorOfTransaction(readWriteSpan, logCPUDemand, logHeapDemand, logDiskDemand);
        }
    }

    @Override
    public boolean isOnEndingRequired() {
        return true;
    }
}
