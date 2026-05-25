package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class PipelineRunnerHeartbeatRequest {
    private String runnerName;

    private String runnerToken;

    private Integer currentConcurrency;
}
