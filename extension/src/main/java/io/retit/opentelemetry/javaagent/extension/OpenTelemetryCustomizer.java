package io.retit.opentelemetry.javaagent.extension;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.processor.RETITSpanProcessor;
import io.retit.opentelemetry.javaagent.extension.resources.JavaAgentGCHandler;

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
