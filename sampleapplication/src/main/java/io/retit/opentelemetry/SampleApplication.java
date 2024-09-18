package io.retit.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.logging.Logger;

/**
 * The purpose of this class is to collect spans and metrics for a very simple use case.
 * The class is used in the integration test called JavaAgentExtensionIntegrationTest.
 */
public class SampleApplication {

    private static final Logger LOGGER = Logger.getLogger(SampleApplication.class.getName());

    private static final String CONTINUOUS_RUN_MODE = "continuously";

    /**
     * Simple method annotated with @WithSpan to collect Otel data.
     */
    @WithSpan
    public static void method1() {
        LOGGER.info("method1");
    }

    /**
     * Simple method annotated with @WithSpan to collect Otel data.
     */
    @WithSpan
    public static void method2() {
        LOGGER.info("method2");
    }

    /**
     * Main method that calls the other method of the sample application.
     *
     * @param args - not used
     * @throws InterruptedException - in case the application couldn't wait for the metric publishing.
     */
    public static void main(String[] args) throws InterruptedException {
        // Call methods
        if (CONTINUOUS_RUN_MODE.equals(System.getProperty("RUN_MODE"))) {
            while (true) {
                businessMethod();
                Thread.sleep(500);
            }
        } else {
            businessMethod();
        }

        if (System.getenv("WAIT_FOR_ONE_MINUTE") != null) {
            // we need to wait for 60 s so that the metrics are at least published once
            Thread.sleep(60_000);
        }
    }

    private static void businessMethod() {
        Span span = Span.current();
        span.setAttribute("test", "some value");
        method1();
        method2();
        span.end();
    }
}