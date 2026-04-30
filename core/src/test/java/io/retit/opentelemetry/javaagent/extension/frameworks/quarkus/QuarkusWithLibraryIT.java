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

package io.retit.opentelemetry.javaagent.extension.frameworks.quarkus;

import io.retit.opentelemetry.javaagent.extension.frameworks.AbstractFrameworkIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the quarkus-rest-service-library example.
 */
public class QuarkusWithLibraryIT extends AbstractFrameworkIT {

    @BeforeEach
    public void beforeEach() {
        commonSetupForLibrary("quarkus-rest-service-library:feature", "quarkus-library-app", 8080);
    }

    @Test
    public void testCallEachEndpointAndAssertSpansAndMetrics() {
        super.testCallEachEndpointAndAsserSpansAndMetrics();
    }
}
