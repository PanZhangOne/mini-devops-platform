package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过 100个字符")
    private String name;

    @NotBlank(message = "项目编码不能为空")
    @Size(max = 100, message = "项目编码长度不能超过 100")
    private String code;

    private String description;

    private Long ownerId;
}
