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

package io.retit.opentelemetry.javaagent.extension;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.processor.RETITSpanProcessor;
import io.retit.opentelemetry.javaagent.extension.resources.jvm.JavaAgentGCHandler;

import java.util.logging.Logger;

/**
 * The OpenTelemetryCustomizer is responsible for adding the resource demand carbon emission extensions to the Otel agent.
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class OpenTelemetryCustomizer implements AutoConfigurationCustomizerProvider {

    private static final Logger LOGGER = Logger.getLogger(OpenTelemetryCustomizer.class.getName());

    @Override
    public void customize(final AutoConfigurationCustomizer autoConfiguration) {
        if (InstanceConfiguration.isLogGCEventDefaultTrue()) {
            JavaAgentGCHandler.addJavaAgentGCListener();
        }

        // TracerProviderCustomizer call must be wrapped inside a customizer call which happens after it.
        // Otherwise we would not get the autoconfigured SpanExporter
        autoConfiguration.addSpanExporterCustomizer((delegate, unused) -> {
            autoConfiguration.addTracerProviderCustomizer(this::configureSdkTracerProvider);
            return delegate;
        });
    }

    /**
     * Prepares the list of {@link SpanProcessor} in the {@link SdkTracerProviderBuilder} to include a {@link RETITSpanProcessor}.
     *
     * @param tracerProvider - preconfigured {@link SdkTracerProviderBuilder} from auto-configuration
     * @param config         - preconfigured {@link ConfigProperties} from auto-configuration
     * @return {@link SdkTracerProviderBuilder} with adjusted SdkTracerProviderBuilder
     */
    private SdkTracerProviderBuilder configureSdkTracerProvider(
            final SdkTracerProviderBuilder tracerProvider, final ConfigProperties config) {

        LOGGER.info("Adding RETITSpanProcessor to tracerProvider " + tracerProvider + "with config " + config);
        return tracerProvider
                .addSpanProcessor(new RETITSpanProcessor());
    }
}
