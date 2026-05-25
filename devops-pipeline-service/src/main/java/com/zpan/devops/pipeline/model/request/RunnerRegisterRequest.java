package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RunnerRegisterRequest {
    @NotBlank(message = "Runner名称不能为空")
    @Size(max = 100, message = "Runner名称长度不能超过 100")
    private String runnerName;

    @NotBlank(message = "Runner Token不能为空")
    @Size(max = 200, message = "Runner Token长度不能超过 200")
    private String runnerToken;

    @Size(max = 100, message = "Runner IP长度不能超过 100")
    private String ip;

    private Integer port;

    @NotNull(message = "最大并发数不能为空")
    @Min(value = 1, message = "最大并发数不能小于 1")
    @Max(value = 100, message = "最大并发数不能大于 100")
    private Integer maxConcurrency;
}
