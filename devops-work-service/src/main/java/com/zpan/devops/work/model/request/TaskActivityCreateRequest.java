package com.zpan.devops.work.model.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskActivityCreateRequest {

    private Long taskId;

    private String actionType;

    private String actionContent;

    private String oldValue;

    private String newValue;

    private Long createdBy;

    private LocalDateTime createdAt;
}
