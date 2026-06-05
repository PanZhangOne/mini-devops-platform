package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskActivityVO {

    private Long id;

    private Long taskId;

    private String actionType;

    private String actionTypeDescription;

    private String actionContent;

    private String oldValue;

    private String oldValueDescription;

    private String newValue;

    private String newValueDescription;

    private Long createdBy;

    private LocalDateTime createdAt;
}
