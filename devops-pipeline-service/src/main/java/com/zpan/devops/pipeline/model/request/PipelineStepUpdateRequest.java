package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PipelineStepUpdateRequest {
    @NotBlank(message = "步骤名称不能为空")
    @Size(max = 100, message = "步骤名称长度不能超过 100")
    private String name;

    @NotBlank(message = "步骤类型不能为空")
    private String stepType;

    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;

    private String command;

    private String configJson;

    private Integer timeoutSeconds;

    private Boolean enabled;
}
