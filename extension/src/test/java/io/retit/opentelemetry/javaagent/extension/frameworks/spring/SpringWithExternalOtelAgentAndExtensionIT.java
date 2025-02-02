package io.retit.opentelemetry.javaagent.extension.frameworks.spring;

import io.retit.opentelemetry.javaagent.extension.frameworks.AbstractFrameworkIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SpringWithExternalOtelAgentAndExtensionIT extends AbstractFrameworkIT {
    @BeforeEach
    public void beforeEach() {
        commonSetup("spring-rest-service:feature", "spring-app", 8081, true, true);
    }

    /**
     * This test will run continuously until it is manually stopped, so it is disabled by default.
     */
    @Disabled
    @Test
    public void runTestContinuously() {
        super.runTestContinuously();
    }
}
