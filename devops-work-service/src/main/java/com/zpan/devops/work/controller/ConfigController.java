package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ConfigController {

    private final WorkProperties workProperties;

    public ConfigController(WorkProperties workProperties) {
        this.workProperties = workProperties;
    }

    @GetMapping("/config")
    public Result<WorkProperties> config() {
        return Result.success(workProperties);
    }

    @Data
    @RefreshScope
    @ConfigurationProperties(prefix = "devops.work")
    public static class WorkProperties {

        private String welcomeMessage;
    }
}
