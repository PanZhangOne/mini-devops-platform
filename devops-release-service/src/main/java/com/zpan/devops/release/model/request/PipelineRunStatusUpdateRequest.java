package com.zpan.devops.release.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PipelineRunStatusUpdateRequest {

    @NotBlank(message = "流水线运行状态不能为空")
    private String status;

    @Size(max = 300, message = "镜像标签长度不能超过 300")
    private String imageTag;

    private String logText;

    private String errorMessage;
}
