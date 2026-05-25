package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class PipelineRunnerRegisterRequest {
    private String runnerName;

    private String runnerToken;

    private String ip;

    private Integer port;

    private Integer maxConcurrency;
}
