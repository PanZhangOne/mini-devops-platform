package com.zpan.devops.pipeline;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.zpan.devops.pipeline.client")
@MapperScan("com.zpan.devops.pipeline.mapper")
@ConfigurationPropertiesScan("com.zpan.devops")
@SpringBootApplication(scanBasePackages = "com.zpan.devops")
public class DevopsPipelineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsPipelineServiceApplication.class, args);
    }
}
