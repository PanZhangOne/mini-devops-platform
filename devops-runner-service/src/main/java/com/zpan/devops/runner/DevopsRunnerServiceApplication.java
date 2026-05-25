package com.zpan.devops.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients(basePackages = "com.zpan.devops.runner.client")
@ConfigurationPropertiesScan("com.zpan.devops")
@SpringBootApplication(scanBasePackages = "com.zpan.devops")
public class DevopsRunnerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsRunnerServiceApplication.class, args);
    }
}