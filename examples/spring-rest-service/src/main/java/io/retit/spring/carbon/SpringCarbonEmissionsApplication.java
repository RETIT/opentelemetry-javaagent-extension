package io.retit.spring.carbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Spring-based carbon emissions demo application.
 */
@EnableScheduling
@SpringBootApplication
public class SpringCarbonEmissionsApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(SpringCarbonEmissionsApplication.class, args);
    }

}
