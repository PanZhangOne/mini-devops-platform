package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RunnerTaskStepFinishRequest {

    @NotBlank(message = "Runner名称不能为空")
    private String runnerName;

    @NotBlank(message = "Runner Token不能为空")
    private String runnerToken;

    @NotBlank(message = "步骤状态不能为空")
    private String status;

    @NotNull(message = "退出码不能为空")
    private Integer exitCode;

    private String errorMessage;
}
