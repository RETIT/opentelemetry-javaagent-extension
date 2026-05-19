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
import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_CPU_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_HEAP_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_DISK_DEMAND_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_RESPONSE_TIME_LOGGING_CONFIGURATION_PROPERTY, true);
        InstanceConfiguration.setBooleanProperty(Constants.RETIT_THREAD_NAME_LOGGING_CONFIGURATION_PROPERTY, true);
        retitSpanProcessor = new RETITSpanProcessor();
        //retitSpanProcessor.buildBatchSpanProcessor();
        //Assertions.assertNotNull(retitSpanProcessor.getDelegateBatchSpanProcessor());
        spanData = dummySpanData(Attributes.empty());

        readableSpan = mock(ReadableSpan.class);
        readWriteSpan = mock(ReadWriteSpan.class);
    }

    @Test
    public void onStart() {
        retitSpanProcessor.onStart(Context.root(), readWriteSpan);
        verify(readWriteSpan, times(6)).setAttribute(Mockito.anyString(), Mockito.anyLong());
    }

    @Test
    public void onEnd() {
        when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
        when(readableSpan.toSpanData()).thenReturn(spanData);

        retitSpanProcessor.onEnd(readableSpan);
        verify(readableSpan, atMostOnce()).toSpanData();
    }

    @Test
    public void close() {
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
}
