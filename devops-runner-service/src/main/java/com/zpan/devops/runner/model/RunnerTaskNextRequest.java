package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class RunnerTaskNextRequest {
    private String runnerName;

    private String runnerToken;
}
