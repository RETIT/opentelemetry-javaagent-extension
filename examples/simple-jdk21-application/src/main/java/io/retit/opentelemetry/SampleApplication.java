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

    private static final String VIRTUAL_THREAD = "VIRTUAL_THREAD";

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
        } else if (VIRTUAL_THREAD.equals(System.getenv("RUN_MODE"))) {
            LOGGER.info("RUNNING WITH VIRTUAL THREAD");
            Thread.ofVirtual().start(() -> {
                try {
                    sampleApplication.businessMethod();
                } catch (InterruptedException | IOException e) {
                    LOGGER.severe("Business method invocation failed: " + e.getMessage());
                }

            }).join();
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