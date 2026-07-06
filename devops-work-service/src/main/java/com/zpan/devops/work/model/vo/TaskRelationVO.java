package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRelationVO {

    private Long id;

    private Long taskId;

    private String relationType;

    private String relationTypeDescription;

    private Long relationId;

    private String relationKey;

    private String relationTitle;

    private LocalDateTime createdAt;
}
