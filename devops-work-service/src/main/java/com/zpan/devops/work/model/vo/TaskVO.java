package com.zpan.devops.work.model.vo;

import lombok.Data;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskVO {
    private Long id;

    private Long projectId;

    private Long moduleId;

    private Long parentTaskId;

    private String taskNo;

    private String title;

    private String description;

    private String taskType;

    private String taskTypeDescription;

    private String status;

    private String statusDescription;

    private String priority;

    private String priorityDescription;

    private Long assigneeId;

    private Long reporterId;

    private String reporterName;

    private LocalDateTime dueDate;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private BigDecimal estimatedHours;

    private BigDecimal actualHours;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
