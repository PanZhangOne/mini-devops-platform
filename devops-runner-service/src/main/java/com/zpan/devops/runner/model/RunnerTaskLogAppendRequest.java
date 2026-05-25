package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class RunnerTaskLogAppendRequest {
    private String runnerName;

    private String runnerToken;

    private Long stepRunId;

    private String logLevel;

    private String content;
}
