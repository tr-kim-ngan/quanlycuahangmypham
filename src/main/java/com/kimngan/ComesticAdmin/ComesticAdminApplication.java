package com.kimngan.ComesticAdmin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan

public class ComesticAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComesticAdminApplication.class, args);
    }
}