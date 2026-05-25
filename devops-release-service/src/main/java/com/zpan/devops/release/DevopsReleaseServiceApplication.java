package com.zpan.devops.release;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.zpan.devops.release.client")
@MapperScan("com.zpan.devops.release.mapper")
@ConfigurationPropertiesScan("com.zpan.devops")
@SpringBootApplication(scanBasePackages = "com.zpan.devops")
public class DevopsReleaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsReleaseServiceApplication.class, args);
    }
}
