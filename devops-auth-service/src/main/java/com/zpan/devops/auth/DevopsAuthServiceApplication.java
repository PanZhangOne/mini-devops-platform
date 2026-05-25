package com.zpan.devops.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@MapperScan("com.zpan.devops.auth.mapper")
@ConfigurationPropertiesScan("com.zpan.devops")
@SpringBootApplication(scanBasePackages = "com.zpan.devops")
public class DevopsAuthServiceApplication {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(DevopsAuthServiceApplication.class, args);
    }
}
