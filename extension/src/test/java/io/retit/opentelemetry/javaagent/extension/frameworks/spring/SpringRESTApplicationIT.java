package io.retit.opentelemetry.javaagent.extension.frameworks.spring;

import io.retit.opentelemetry.javaagent.extension.frameworks.AbstractFrameworkIT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SpringRESTApplicationIT extends AbstractFrameworkIT {
    /**
     * This test will run continuously until it is manually stopped, so it is disabled by default.
     */
    @Disabled
    @Test
    public void runTestContinuously() {
        // this test is meant to be executed for a Spring app that has been started by the user.
        CONTAINER_URL = "http://localhost:8081/";
        super.runTestContinuously();
    }
}
