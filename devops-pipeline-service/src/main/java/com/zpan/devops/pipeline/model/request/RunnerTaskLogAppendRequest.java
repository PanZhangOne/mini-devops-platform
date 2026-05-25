package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunnerTaskLogAppendRequest {

    @NotBlank(message = "Runner名称不能为空")
    private String runnerName;

    @NotBlank(message = "Runner Token不能为空")
    private String runnerToken;

    private Long stepRunId;

    @NotBlank(message = "日志级别不能为空")
    private String logLevel;

    @NotBlank(message = "日志内容不能为空")
    private String content;
}
