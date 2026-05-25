package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class RunnerTaskStepFinishRequest {

    private String runnerName;

    private String runnerToken;

    private String status;

    private Integer exitCode;

    private String errorMessage;
}
