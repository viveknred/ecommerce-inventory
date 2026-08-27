package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Application entry point.
 *
 * <p>{@code @ConfigurationPropertiesScan} registers the {@code @ConfigurationProperties}
 * beans in this package tree, which from Phase 4 includes
 * {@code MediaProperties} for the upload limits.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EcommerceInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceInventoryApplication.class, args);
    }
}
