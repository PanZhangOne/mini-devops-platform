package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskStatusChangeRequest {

    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    private Long userId;

    @Size(max = 500, message = "备注内容不能超过500个字符")
    private String remark;
}
