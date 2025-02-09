package io.retit.spring.carbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpringCarbonEmissionsApplication {

    public static void main(final String[] args) {
        SpringApplication.run(SpringCarbonEmissionsApplication.class, args);
    }

}
