package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCommentVO {

    private Long id;

    private Long taskId;

    private Long parentId;

    private String content;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
