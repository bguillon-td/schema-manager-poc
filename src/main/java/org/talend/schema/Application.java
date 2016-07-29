package org.talend.schema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({ "org.talend.schema" })

public class Application {

    public Application() { // NOSONAR
        // empty constructor for Spring boot application class
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args); // NOSONAR
    }

}
