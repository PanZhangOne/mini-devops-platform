package com.zpan.devops.code.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepositoryCreateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称长度不能超过100")
    private String repoName;

    @NotBlank(message = "仓库地址不能为空")
    private String repoUrl;

    @NotBlank(message = "默认分支不能为空")
    @Size(max = 100, message = "默认分支字符最大不能超过100")
    private String defaultBranch;

    @NotBlank(message = "仓库类型不能为空")
    private String repoType;

    @Size(max = 500, message = "仓库描述长度不能超过500")
    private String description;
}
