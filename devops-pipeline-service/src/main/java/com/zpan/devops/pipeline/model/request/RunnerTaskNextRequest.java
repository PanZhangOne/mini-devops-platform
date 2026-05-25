package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunnerTaskNextRequest {

    @NotBlank(message = "Runner名称不能为空")
    private String runnerName;

    @NotBlank(message = "Runner Token不能为空")
    private String runnerToken;
}
