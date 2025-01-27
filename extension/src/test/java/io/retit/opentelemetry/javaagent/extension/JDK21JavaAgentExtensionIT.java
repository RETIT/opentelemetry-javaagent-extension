package io.retit.opentelemetry.javaagent.extension;

import org.junit.jupiter.api.BeforeEach;

public class JDK21JavaAgentExtensionIT extends JavaAgentExtensionIT {

    @BeforeEach
    public void setupApplication() {
        String image = "simple-jdk21-application:feature";
        commonSetup(image);
    }
}
