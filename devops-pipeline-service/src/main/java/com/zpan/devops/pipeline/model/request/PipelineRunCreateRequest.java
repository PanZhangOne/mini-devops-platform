package com.zpan.devops.pipeline.model.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PipelineRunCreateRequest {


    private Long versionId;

    @Size(max = 100, message = "分支名称长度不能超过 100")
    private String branchName;

    @Size(max = 100, message = "提交哈希长度不能超过 100")
    private String commitHash;

    @Size(max = 300, message = "镜像标签长度不能超过 300")
    private String imageTag;

    @Size(max = 30, message = "环境长度不能超过 30")
    private String env;
}
