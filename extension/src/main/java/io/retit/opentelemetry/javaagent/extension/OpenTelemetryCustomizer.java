package io.retit.opentelemetry.javaagent.extension;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.retit.opentelemetry.javaagent.extension.processor.RETITSpanProcessor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            autoConfiguration.addTracerProviderCustomizer(prepRetitSpanProcessor(delegate));
            return delegate;
        });
    }

    /**
     * Prepares the list of {@link SpanProcessor} in the {@link SdkTracerProviderBuilder} to only include a {@link RETITSpanProcessor}
     * with {@link BatchSpanProcessorBuilder}. For every {@link SpanExporter} defined, that spanExporter will replace the existing spanExporter in the
     * BatchSpanProcessorBuilder as a composite SpanExporter. After all configured spanExporters have been added, the RETITSpanProcessor will build
     * the actual {@link BatchSpanProcessor} to be used.
     * <p>
     * This is done through reflection because otherwise we do not have access to the created SpanProcessors. All of this is necessary as it is not
     * possible to set attributes during the onEnd hook of the SpanProcessor.
     *
     * @param spanExporter - preconfigured {@link SpanExporter} from auto-configuration
     * @return {@link BiFunction} with adjusted SdkTracerProviderBuilder
     */
    private BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder> prepRetitSpanProcessor(SpanExporter spanExporter) {
        return (sdkTracerProviderBuilder, configProperties) -> {
            try {
                final Class<?> sdkTracerProviderBuilderClass =
                        SdkTracerProviderBuilder.class.getClassLoader().loadClass("io.opentelemetry.sdk.trace.SdkTracerProviderBuilder");
                final Field spanProcessors = sdkTracerProviderBuilderClass.getDeclaredField("spanProcessors");
                spanProcessors.setAccessible(true);
                List<SpanProcessor> spanProcessorList = (List<SpanProcessor>) spanProcessors.get(sdkTracerProviderBuilder);
                List<SpanProcessor> resultList = spanProcessorList.stream().filter(RETITSpanProcessor.class::isInstance).collect(Collectors.toList());

                if (!resultList.isEmpty()) {
                    RETITSpanProcessor retitSpanProcessor = (RETITSpanProcessor) resultList.get(0);
                    BatchSpanProcessorBuilder delegateBatchSpanProcessorBuilder = retitSpanProcessor.getDelegateBatchSpanProcessorBuilder();
                    final Class<?> batchSpanProcessorBuilderClass =
                            BatchSpanProcessorBuilder.class.getClassLoader().loadClass("io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder");
                    final Field spanExporterField = batchSpanProcessorBuilderClass.getDeclaredField("spanExporter");
                    spanExporterField.setAccessible(true);
                    SpanExporter currentSpanExporter = (SpanExporter) spanExporterField.get(delegateBatchSpanProcessorBuilder);
                    spanExporterField.set(delegateBatchSpanProcessorBuilder, SpanExporter.composite(currentSpanExporter, spanExporter));
                    retitSpanProcessor.buildBatchSpanProcessor();
                } else {
                    RETITSpanProcessor newRetitSpanProcessor = new RETITSpanProcessor(BatchSpanProcessor.builder(spanExporter));
                    newRetitSpanProcessor.buildBatchSpanProcessor();
                    resultList.add(newRetitSpanProcessor);
                }

                spanProcessors.set(sdkTracerProviderBuilder, resultList);
            } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Could not remove preconfigured SpanProcessor", e);
            }
            return sdkTracerProviderBuilder;
        };
    }
}
