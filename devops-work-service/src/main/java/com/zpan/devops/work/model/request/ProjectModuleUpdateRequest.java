package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectModuleUpdateRequest {
    private Long parentId;

    @NotBlank(message = "模块名称不能为空")
    @Size(max = 100, message = "模块名称长度不能超过 100")
    private String name;

    @NotBlank(message = "模块编码不能为空")
    @Size(max = 100, message = "模块编码长度不能超过 100")
    private String code;

    @Size(max = 500, message = "模块描述长度不能超过 500")
    private String description;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sortOrder;
}
