package com.zpan.devops.code.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepositoryUpdateRequest {
    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称长度不能超过 100")
    private String repoName;

    @NotBlank(message = "默认分支不能为空")
    @Size(max = 100, message = "默认分支长度不能超过 100")
    private String defaultBranch;

    @NotBlank(message = "仓库类型不能为空")
    private String repoType;

    @Size(max = 500, message = "仓库描述长度不能超过 500")
    private String description;
}
