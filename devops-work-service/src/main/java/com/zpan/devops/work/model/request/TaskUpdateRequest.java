package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题长度不能超过 200")
    private String title;

    private String description;

    private Long assigneeId;

    @NotBlank(message = "任务状态不能为空")
    private String status;

    @NotBlank(message = "任务优先级不能为空")
    private String priority;

    private LocalDateTime deadline;
}
