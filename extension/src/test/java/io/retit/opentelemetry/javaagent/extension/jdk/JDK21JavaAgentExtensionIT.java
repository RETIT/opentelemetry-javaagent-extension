package io.retit.opentelemetry.javaagent.extension.jdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JDK21JavaAgentExtensionIT extends JavaAgentExtensionIT {

    @BeforeEach
    public void setupApplication() {
        String image = "simple-jdk21-application:feature";
        commonSetup(image);
    }

    @Test
    public void testCPUDemandWithVirtualThreads() {
        applicationContainer.withEnv("RUN_MODE", "VIRTUAL_THREAD");
        testOnlyCPUDemand();
    }
}
