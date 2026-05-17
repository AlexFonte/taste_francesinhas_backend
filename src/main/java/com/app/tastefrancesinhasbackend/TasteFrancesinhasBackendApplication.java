package com.app.tastefrancesinhasbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TasteFrancesinhasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TasteFrancesinhasBackendApplication.class, args);
    }

}