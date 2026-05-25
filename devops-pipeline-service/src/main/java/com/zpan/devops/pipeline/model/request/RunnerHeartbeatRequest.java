package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunnerHeartbeatRequest {


    @NotBlank(message = "Runner名称不能为空")
    private String runnerName;

    @NotBlank(message = "Runner Token不能为空")
    private String runnerToken;

    @Min(value = 0, message = "当前并发数不能小于 0")
    private Integer currentConcurrency;
}
