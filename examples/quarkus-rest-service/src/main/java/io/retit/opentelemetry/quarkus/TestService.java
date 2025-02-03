package io.retit.opentelemetry.quarkus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example service that does some very complex processing.
 */
@ApplicationScoped
public class TestService {

    @Inject
    private ResourceDemandMeasurementService resourceDemandMeasurementService;

    public String veryComplexBusinessFunction(final int size, final String httpMethod) throws InterruptedException, IOException {
        ResourceDemandMeasurementService.Measurement measurement = resourceDemandMeasurementService.measure();
        Path tempFile = Files.createTempFile("sampleapplication", "veryComplexBusinessFunction");

        int[] data = naiveSortingWithONSquareComplexity(generateRandomInputArray(size));

        Files.write(tempFile, String.valueOf(data).getBytes());

        Files.delete(tempFile);
        resourceDemandMeasurementService.measureAndPublishMetrics(measurement, httpMethod);
        return String.valueOf(Arrays.stream(data).sum());
    }

    private static int[] generateRandomInputArray(final int size) throws InterruptedException {
        int array[] = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = ThreadLocalRandom.current().nextInt(0, size * 10);
        }

        Thread.sleep(16);
        return array;
    }

    // O(n²)
    private static int[] naiveSortingWithONSquareComplexity(final int... inputArray) {
        // Outer loop
        for (int i = 0; i < inputArray.length; i++) {

            // Inner nested loop pointing 1 index ahead
            for (int j = i + 1; j < inputArray.length; j++) {

                // Checking elements
                int temp;
                if (inputArray[j] < inputArray[i]) {

                    // Swapping
                    temp = inputArray[i];
                    inputArray[i] = inputArray[j];
                    inputArray[j] = temp;
                }
            }

        }
        return inputArray;
    }
}
