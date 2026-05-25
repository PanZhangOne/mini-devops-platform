package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class RunnerTaskStepStartRequest {
    private String runnerName;

    private String runnerToken;
}
