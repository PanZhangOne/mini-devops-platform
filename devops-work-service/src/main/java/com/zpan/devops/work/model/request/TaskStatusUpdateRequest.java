package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskStatusUpdateRequest {
    @NotBlank(message = "任务状态不能为空")
    private String status;
}
