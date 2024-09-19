/*
 *   Copyright 2024 RETIT GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
            MetricPublishingService.getInstance().publishResourceDemandVectorOfTransaction(readWriteSpan, logCPUDemand, logHeapDemand, logDiskDemand, logNetworkDemand);
        }
    }

    @Override
    public boolean isOnEndingRequired() {
        return true;
    }
}
