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

package io.retit.opentelemetry.javaagent.extension.cdi;

import io.opentelemetry.sdk.trace.SpanProcessor;
import io.retit.opentelemetry.javaagent.extension.commons.InstanceConfiguration;
import io.retit.opentelemetry.javaagent.extension.processor.RETITSpanProcessor;
import io.retit.opentelemetry.javaagent.extension.resources.jvm.JavaAgentGCHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;

import java.util.logging.Logger;

/**
 * CDI configuration class that integrates the RETIT OpenTelemetry instrumentation library into
 * any CDI-capable framework (Quarkus, WildFly, Payara, …) <em>without</em> requiring the Java agent.
 *
 * <h2>Quarkus</h2>
 * <p>The {@code quarkus-opentelemetry} extension automatically picks up every CDI bean of type
 * {@link SpanProcessor} and adds it to the tracing pipeline.  Because this library ships a
 * {@code META-INF/beans.xml} (discovery-mode {@code annotated}), Quarkus discovers this class
 * automatically when the library JAR is on the classpath.  No additional configuration or
 * boilerplate code is required in the application.</p>
 *
 * <h2>Other CDI containers</h2>
 * <p>The same auto-discovery mechanism works for any CDI 3.0+ compatible container that honours
 * {@code META-INF/beans.xml} inside library JARs (WildFly, GlassFish, Liberty, …).
 * Consult the container's documentation on how to register a custom {@link SpanProcessor}.</p>
 *
 * <h2>Configuration</h2>
 * <p>Use the same system properties or environment variables documented in the project README,
 * e.g.:</p>
 * <pre>
 *   IO_RETIT_EMISSIONS_CLOUD_PROVIDER=AWS
 *   IO_RETIT_EMISSIONS_CLOUD_PROVIDER_REGION=eu-central-1
 *   IO_RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE=m5.large
 * </pre>
 */
@ApplicationScoped
public class RETITSpanProcessorConfiguration {
    private static final Logger LOGGER = Logger.getLogger(RETITSpanProcessorConfiguration.class.getName());

    @Inject
    Config config;

    /**
     * Produces the {@link RETITSpanProcessor} as an application-scoped CDI bean.
     *
     * <p>Quarkus OpenTelemetry discovers this bean automatically and adds it to the
     * {@code SdkTracerProvider} pipeline.  The processor measures CPU, heap, disk and
     * network resource demand at span start and end, and enriches spans with those values as
     * span attributes.</p>
     *
     * <p>GC-event tracking (controlled by the {@code io.retit.log.gc.event} property, default
     * {@code true}) is initialised here so that GC pressure is captured from application
     * startup onwards.</p>
     *
     * @return a configured {@link RETITSpanProcessor} instance.
     */
    @Produces
    @Dependent // important so that this bean is not proxied as instanceof ExtendedSpanProcessor checks are not working otherwise
    public SpanProcessor retitSpanProcessor() {
        for (String name : config.getPropertyNames()) {
            if (name.startsWith("io.retit.")) {
                config.getOptionalValue(name, String.class)
                        .ifPresent(value -> {
                            LOGGER.info(String.format("Retit config property: %s=%s", name, value));
                            System.setProperty(name, value);
                        });
            }
        }

        if (InstanceConfiguration.isLogGCEventDefaultTrue()) {
            JavaAgentGCHandler.addJavaAgentGCListener();
        }

        LOGGER.info("Adding RETITSpanProcessor as CDI Bean");

        return new RETITSpanProcessor();
    }
}

