package io.retit.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
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
    public void method1() throws InterruptedException, IOException {
        doWork("method1", 500, 16); // windows updates the CPU times only every 15ms
        LOGGER.info("method1");
    }

    /**
     * Simple method annotated with @WithSpan to collect Otel data.
     */
    @WithSpan
    public void method2() throws InterruptedException, IOException {
        doWork("method2", 1000, 16); // windows updates the CPU times only every 15ms
        LOGGER.info("method2");
    }

    private void doWork(final String method, final int amountOfWork, final long sleepTimeInMs) throws InterruptedException, IOException {

        Path tempFile = Files.createTempFile("sampleapplication", method);

        for (int i = 0; i < amountOfWork; i++) {
            // Random requires work on the CPU and Memory
            int randomNum = ThreadLocalRandom.current().nextInt(0, amountOfWork * 10);
            // Writing data requires work on Storage and Memory
            Files.write(tempFile, (method + randomNum).getBytes(StandardCharsets.UTF_8));
            // TODO add network demand
        }

        Files.delete(tempFile);
        Thread.sleep(sleepTimeInMs);
    }

    /**
     * Main method that calls the other method of the sample application.
     *
     * @param args - not used
     * @throws InterruptedException - in case the application couldn't wait for the metric publishing.
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        SampleApplication sampleApplication = new SampleApplication();
        // Call methods
        if (CONTINUOUS_RUN_MODE.equals(System.getProperty("RUN_MODE"))) {
            while (true) {
                sampleApplication.businessMethod();
                Thread.sleep(500);
            }
        } else {
            sampleApplication.businessMethod();
        }

        if (System.getenv("WAIT_FOR_ONE_MINUTE") != null) {
            // we need to wait for 60 s so that the metrics are at least published once
            Thread.sleep(60_000);
        }
    }

    private void businessMethod() throws InterruptedException, IOException {
        Span span = Span.current();
        span.setAttribute("test", "some value");
        method1();
        method2();
        span.end();
    }
}