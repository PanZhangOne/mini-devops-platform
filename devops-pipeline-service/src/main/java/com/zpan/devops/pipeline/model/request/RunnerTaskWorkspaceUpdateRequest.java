package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RunnerTaskWorkspaceUpdateRequest {

    @NotBlank(message = "Runner 名称不能为空")
    private String runnerName;

    @NotBlank(message = "Runner Token不能为空")
    private String runnerToken;

    @NotBlank(message = "工作目录不能为空")
    @Size(max = 500, message = "工作目录长度不能超过 500")
    private String workspaceDir;
}
