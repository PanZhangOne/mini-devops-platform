package com.zpan.devops.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan("com.zpan.devops")
@SpringBootApplication(scanBasePackages = "com.zpan.devops")
public class DevopsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsGatewayApplication.class, args);
    }
}
