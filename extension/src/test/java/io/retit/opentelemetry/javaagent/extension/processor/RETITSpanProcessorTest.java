package io.retit.opentelemetry.javaagent.extension.processor;

import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.InstanceConfiguration;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class RETITSpanProcessorTest {

    private static final SpanContext SAMPLED_SPAN_CONTEXT =
            SpanContext.create(TraceId.getInvalid(), SpanId.getInvalid(), TraceFlags.getSampled(), TraceState.getDefault());

    private ReadableSpan readableSpan;

    private ReadWriteSpan readWriteSpan;

    private RETITSpanProcessor retitSpanProcessor;

    private SpanData spanData;

    @BeforeEach
    public void setup() {
        skipOnMacOS();
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_THREAD_NAME_LOGGING_CONFIGURATION_PROPERTY, true);
        retitSpanProcessor = new RETITSpanProcessor(BatchSpanProcessor.builder(SpanExporter.composite()));
        retitSpanProcessor.buildBatchSpanProcessor();
        assertNotNull(retitSpanProcessor.getDelegateBatchSpanProcessor());
        spanData = dummySpanData(Attributes.empty());

        readableSpan = mock(ReadableSpan.class);
        readWriteSpan = mock(ReadWriteSpan.class);
    }

    @Test
    public void onStart() {
        skipOnMacOS();
        retitSpanProcessor.onStart(Context.root(), readWriteSpan);
        verify(readWriteSpan, times(6)).setAttribute(Mockito.anyString(), Mockito.anyLong());
    }

    @Test
    public void onEnd() {
        skipOnMacOS();
        when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
        when(readableSpan.toSpanData()).thenReturn(spanData);

        retitSpanProcessor.onEnd(readableSpan);
        verify(readableSpan, atMostOnce()).toSpanData();
    }

    @Test
    public void close() {
        skipOnMacOS();
        retitSpanProcessor.close();
    }

    private SpanData dummySpanData(Attributes attributes) {
        return TestSpanData.builder()
                .setHasEnded(true)
                .setSpanContext(SAMPLED_SPAN_CONTEXT)
                .setName("span")
                .setKind(SpanKind.SERVER)
                .setStartEpochNanos(System.nanoTime())
                .setStatus(StatusData.ok())
                .setEndEpochNanos(System.nanoTime())
                .setAttributes(attributes)
                .setTotalRecordedLinks(0)
                .setTotalRecordedEvents(0)
                .build();
    }

    /* Resource demand collection for macOS is not supported.
     * Thus, all of the tests are skipped on macOS to be able to build the project locally.
     */
    private void skipOnMacOS() {
        Assertions.assertFalse(System.getProperty("os.name").toLowerCase().contains("mac"));
    }
}
