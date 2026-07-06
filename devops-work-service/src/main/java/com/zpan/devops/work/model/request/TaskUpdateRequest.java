package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskUpdateRequest {

    private Long moduleId;

    private Long parentTaskId;

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题长度不能超过 200")
    private String title;

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    private String description;

    private Long assigneeId;

    private Long reporterId;

    @NotBlank(message = "任务状态不能为空")
    private String status;

    @NotBlank(message = "任务优先级不能为空")
    private String priority;

    private LocalDateTime dueDate;

    @DecimalMin(value = "0.0", message = "估计工时不能小于0")
    private BigDecimal estimatedHours;

    @DecimalMin(value = "0.0", message = "实际工时不能小于0")
    private BigDecimal actualHours;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder;
}
