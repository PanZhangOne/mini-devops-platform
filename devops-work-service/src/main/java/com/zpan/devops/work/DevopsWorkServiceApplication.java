package com.zpan.devops.work;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@MapperScan("com.zpan.devops.work.mapper")
@SpringBootApplication(scanBasePackages = "com.zpan.devops")
@ConfigurationPropertiesScan("com.zpan.devops")
public class DevopsWorkServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsWorkServiceApplication.class, args);
    }
}