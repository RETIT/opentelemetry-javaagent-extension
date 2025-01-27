package io.retit.opentelemetry.javaagent.extension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JDK21JavaAgentExtensionIT extends JavaAgentExtensionIT {

    @BeforeEach
    public void setupApplication() {
        String image = "simple-jdk21-application:feature";
        commonSetup(image);
    }

    @Test
    public void testWithVirtualThreads () {
        applicationContainer.withEnv("RUN_MODE", "VIRTUAL_THREAD");
        testAllAttributes();
    }
}
