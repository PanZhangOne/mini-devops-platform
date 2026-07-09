package com.zpan.devops.code.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepositoryCreateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "命名空间不能为空")
    @Size(max = 100, message = "命名空间字符最大不能超过100")
    private String namespace;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称字符最大不能超过100")
    private String name;

    @NotBlank(message = "仓库路径不能为空")
    @Size(max = 100, message = "仓库路径字符最大不能超过100")
    private String path;

    @Size(max = 500, message = "仓库描述长度不能超过500")
    private String description;

    private String visibility;
}
