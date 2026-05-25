package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PipelineCreateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "代码仓库ID不能为空")
    private Long repositoryId;

    @NotBlank(message = "流水线名称不能为空")
    @Size(max = 100, message = "流水线名称长度不能超过 100")
    private String name;

    @NotBlank(message = "流水线编码不能为空")
    @Size(max = 100, message = "流水线编码长度不能超过 100")
    private String code;

    @Size(max = 500, message = "流水线描述长度不能超过 500")
    private String description;

    @NotBlank(message = "触发类型不能为空")
    private String triggerType;
}
