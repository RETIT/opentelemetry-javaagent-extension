package io.retit.opentelemetry.javaagent.extension.frameworks.quarkus;

import io.retit.opentelemetry.javaagent.extension.frameworks.AbstractFrameworkIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuarkusWithExternalOtelAgentAndExtensionIT extends AbstractFrameworkIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusWithExternalOtelAgentAndExtensionIT.class);

    @BeforeEach
    public void beforeEach() {
        commonSetup("quarkus-rest-service:feature", "quarkus-app", 8080, true, false);
    }

    /**
     * This test will run continuously until it is manually stopped, so it is disabled by default.
     */
    @Disabled
    @Test
    public void runTestContinuously() {
        super.runTestContinuously();
    }

    @Test
    public void testCallEachEndpointAndAssertSpansAndMetrics() {
        super.testCallEachEndpointAndAsserSpansAndMetrics();
    }
}
