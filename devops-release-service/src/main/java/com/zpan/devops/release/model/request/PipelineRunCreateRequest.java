package com.zpan.devops.release.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PipelineRunCreateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "代码仓库ID不能为空")
    private Long repositoryId;

    @NotNull(message = "版本ID不能为空")
    private Long versionId;

    @NotBlank(message = "环境不能为空")
    private String env;

    @Size(max = 300, message = "镜像标签长度不能超过 300")
    private String imageTag;

    @Size(max = 100, message = "提交哈希长度不能超过 100")
    private String commitHash;

    @NotBlank(message = "触发类型不能为空")
    private String triggerType;
}
