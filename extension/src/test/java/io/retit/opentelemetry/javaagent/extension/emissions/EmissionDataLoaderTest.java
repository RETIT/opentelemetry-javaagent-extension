package io.retit.opentelemetry.javaagent.extension.emissions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmissionDataLoaderTest {

    @Test
    public void testEmissionDataLoader() {
        Assertions.assertNotNull(EmissionDataLoader.getConfigInstance());
    }
}
