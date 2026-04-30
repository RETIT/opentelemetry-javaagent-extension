package io.retit.opentelemetry.javaagent.extension.frameworks.spring;

import io.retit.opentelemetry.javaagent.extension.frameworks.AbstractFrameworkIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SpringWithExternalOtelAgentAndExtensionIT extends AbstractFrameworkIT {
    @BeforeEach
    public void beforeEach() {
        commonSetup("spring-rest-service:feature", "spring-app", 8081, true, false);
    }

    @Test
    public void testCallEachEndpointAndAssertSpansAndMetrics() {
        super.testCallEachEndpointAndAsserSpansAndMetrics();
    }
}
