package io.retit.opentelemetry.javaagent.extension.jdk;

import org.junit.jupiter.api.BeforeEach;

public class JDK8JavaAgentExtensionIT extends JavaAgentExtensionIT {

    @BeforeEach
    public void setupApplication() {
        String image = "simple-jdk8-application:feature";
        commonSetup(image);
    }
}
