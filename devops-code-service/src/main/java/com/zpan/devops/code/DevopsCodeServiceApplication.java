package com.zpan.devops.code;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableFeignClients(basePackages = "com.zpan.devops.code.client")
@MapperScan("com.zpan.devops.code.mapper")
@ConfigurationPropertiesScan("com.zpan.devops")
@SpringBootApplication(scanBasePackages = "com.zpan.devops")
public class DevopsCodeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsCodeServiceApplication.class, args);
    }
}
