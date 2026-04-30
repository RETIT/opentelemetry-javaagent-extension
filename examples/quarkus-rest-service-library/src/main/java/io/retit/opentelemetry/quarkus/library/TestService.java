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

package io.retit.opentelemetry.quarkus.library;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example service performing CPU- and I/O-intensive work so that the RETITSpanProcessor
 * has meaningful resource demand data to report.
 */
@ApplicationScoped
public class TestService {

    @Inject
    private ResourceDemandMeasurementService resourceDemandMeasurementService;

    /**
     * Executes a simple sorting algorithm to create measurable CPU and disk load.
     *
     * @param size       array size to sort.
     * @param httpMethod HTTP method to include as metric attribute.
     * @return sum of sorted array as string.
     * @throws InterruptedException if the thread is interrupted.
     * @throws IOException          if a file operation fails.
     */
    public String veryComplexBusinessFunction(final int size, final String httpMethod)
            throws InterruptedException, IOException {
        ResourceDemandMeasurementService.Measurement measurement = resourceDemandMeasurementService.measure();
        Path tempFile = Files.createTempFile("sampleapplication", "veryComplexBusinessFunction");
        try {
            int[] data = naiveSortingWithONSquareComplexity(generateRandomInputArray(size));
            Files.write(tempFile, String.valueOf(data).getBytes(StandardCharsets.UTF_8));
            resourceDemandMeasurementService.measureAndPublishMetrics(measurement, httpMethod);
            return String.valueOf(Arrays.stream(data).sum());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static int[] generateRandomInputArray(final int size) throws InterruptedException {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = ThreadLocalRandom.current().nextInt(0, size * 10);
        }
        Thread.sleep(16);
        return array;
    }

    private static int[] naiveSortingWithONSquareComplexity(final int... inputArray) {
        for (int i = 0; i < inputArray.length; i++) {
            for (int j = i + 1; j < inputArray.length; j++) {
                if (inputArray[j] < inputArray[i]) {
                    int temp = inputArray[i];
                    inputArray[i] = inputArray[j];
                    inputArray[j] = temp;
                }
            }
        }
        return inputArray;
    }
}