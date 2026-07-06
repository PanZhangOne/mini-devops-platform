package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRelationCreateRequest {

    @NotBlank(message = "关联类型不能为空")
    @Size(max = 50, message = "关联类型长度不能超过50")
    private String relationType;

    // 关联对象ID
    private Long relationId;

    @NotBlank(message = "关联对象Key不能为空")
    @Size(max = 200, message = "关联对象Key长度不能超过200")
    private String relationKey;

    @Size(max = 500, message = "关联对象标题长度不能超过500")
    private String relationTitle;
}
