package com.zpan.devops.runner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "runner")
public class RunnerProperties {

    private String name;

    private String token;

    private String ip;

    private Integer port;

    private Integer maxConcurrency;

    private Long heartbeatIntervalMs;

    private Long taskPollIntervalMs;

    private String workspaceRoot;

    // Harbor 的配置可以根据项目来，目前是基于runner的
    private String harborRegistry;

    private String harborUsername;

    private String harborPassword;
}
