package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.commons.TelemetryUtils;
import io.retit.opentelemetry.javaagent.extension.metrics.MetricPublishingService;

/**
 * This is the core of the RETIT OpenTelemetry Extension and measures resource demand at the start and end of each span.
 */
public class RETITSpanProcessor implements ExtendedSpanProcessor {

    @Override
    public void onStart(final Context parentContext, final ReadWriteSpan readWriteSpan) {
        boolean logCPUDemand = InstanceConfiguration.isLogCpuDemandDefaultTrue();
        boolean logHeapDemand = InstanceConfiguration.isLogHeapDemandDefaultTrue();
        boolean logResponseTime = InstanceConfiguration.isLogResponseTime();
        boolean logDiskDemand = InstanceConfiguration.isLogDiskDemand();
        boolean logNetworkDemand = InstanceConfiguration.isLogNetworkDemand();

        TelemetryUtils.addStartResourceDemandValuesToSpanAttributes(
                logCPUDemand,
                logResponseTime,
                logHeapDemand,
                logDiskDemand,
                logNetworkDemand,
                readWriteSpan);
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(final ReadableSpan readableSpan) {
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public void onEnding(final ReadWriteSpan readWriteSpan) {
        boolean logCPUDemand = InstanceConfiguration.isLogCpuDemandDefaultTrue();
        boolean logHeapDemand = InstanceConfiguration.isLogHeapDemandDefaultTrue();
        boolean logResponseTime = InstanceConfiguration.isLogResponseTime();
        boolean logDiskDemand = InstanceConfiguration.isLogDiskDemand();
        boolean logNetworkDemand = InstanceConfiguration.isLogNetworkDemand();

        TelemetryUtils.addEndResourceDemandValuesToSpanAttributes(
                logCPUDemand,
                logResponseTime,
                logHeapDemand,
                logDiskDemand,
                logNetworkDemand,
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
