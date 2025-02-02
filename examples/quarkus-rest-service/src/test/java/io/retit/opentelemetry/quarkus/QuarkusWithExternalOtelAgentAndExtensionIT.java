package io.retit.opentelemetry.quarkus;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuarkusWithExternalOtelAgentAndExtensionIT extends AbstractQuarkusIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusWithExternalOtelAgentAndExtensionIT.class);

    @BeforeEach
    public void beforeEach() {
        super.commonSetup(true, true);
    }

    /**
     * This test will run continuously until it is manually stopped, so it is disabled by default.
     */
    @Disabled
    @Test
    public void runTestContinuously() {
        while (true) {
            RestAssured.given().get(QUARKUS_CONTAINER_URL + "/test-rest-endpoint/getData").then().statusCode(200);
            RestAssured.given().post(QUARKUS_CONTAINER_URL + "/test-rest-endpoint/postData").then().statusCode(200);
            RestAssured.given().delete(QUARKUS_CONTAINER_URL + "/test-rest-endpoint/deleteData").then().statusCode(200);
        }
    }

}
