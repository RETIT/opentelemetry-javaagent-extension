package io.retit.opentelemetry.javaagent.extension.frameworks.quarkus;

import io.retit.opentelemetry.javaagent.extension.frameworks.AbstractFrameworkIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QuarkusWithInternalOtelSupportAndCDILibraryIT extends AbstractFrameworkIT {

    @BeforeEach
    public void beforeEach() {
        commonSetup("quarkus-rest-service-cdi-library:feature", "quarkus-app", 8080, false, false);
    }

    @Test
    public void testCallEachEndpointAndAssertSpansAndMetrics() {
        super.testCallEachEndpointAndAsserSpansAndMetrics();
    }
}
