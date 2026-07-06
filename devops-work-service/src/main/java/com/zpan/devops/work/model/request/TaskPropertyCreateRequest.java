package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskPropertyCreateRequest {

    @NotBlank(message = "属性名称不能为空")
    @Size(max = 100, message = "属性名称长度不能超过 100")
    private String name;

    @NotBlank(message = "属性编码不能为空")
    @Size(max = 100, message = "属性编码长度不能超过 100")
    private String code;

    @NotBlank(message = "属性类型不能为空")
    @Size(max = 30, message = "属性类型长度不能超过 30")
    private String propertyType;

    private Boolean required;

    private String optionsJson;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sortOrder;

    private Boolean enabled;
}
