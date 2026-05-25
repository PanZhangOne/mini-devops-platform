package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskVO {
    private Long id;

    private Long projectId;

    private String title;

    private String description;

    private Long assigneeId;

    private String status;

    private String statusDescription;

    private String priority;

    private String priorityDescription;

    private LocalDateTime deadline;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
