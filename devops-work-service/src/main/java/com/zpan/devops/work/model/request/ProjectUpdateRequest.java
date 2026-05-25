package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectUpdateRequest {
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过 100")
    private String name;

    @Size(max = 500, message = "项目描述长度不能超过 500")
    private String description;

    private Long ownerId;

    @NotBlank(message = "项目状态不能为空")
    private String status;
}
