package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class RunnerTaskFinishRequest {
    private String runnerName;

    private String runnerToken;

    private String status;

    private String errorMessage;
}
