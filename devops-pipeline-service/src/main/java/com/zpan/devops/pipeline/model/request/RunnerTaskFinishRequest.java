package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunnerTaskFinishRequest {
    @NotBlank(message = "Runner名称不能为空")
    private String runnerName;

    @NotBlank(message = "Runner Token不能为空")
    private String runnerToken;

    @NotBlank(message = "流水线状态不能为空")
    private String status;

    private String errorMessage;
}
