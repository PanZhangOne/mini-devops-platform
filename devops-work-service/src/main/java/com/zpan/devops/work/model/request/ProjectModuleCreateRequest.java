package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectModuleCreateRequest {

    @NotNull(message = "父模块ID不能为空")
    private Long parentId;

    @NotBlank(message = "模块名称不能为空")
    @Size(max = 100, min = 2, message = "模块名称长度必须在3到100个字符之间")
    private String name;

    @NotBlank(message = "模块代码不能为空")
    @Size(max = 100, min = 3, message = "模块代码长度必须在3到100个字符之间")
    private String code;

    private String description;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder;
}
