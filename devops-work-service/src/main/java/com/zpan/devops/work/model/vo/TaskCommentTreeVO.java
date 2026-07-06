package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskCommentTreeVO {
    private Long id;

    private Long taskId;

    private Long parentId;

    private String content;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<TaskCommentTreeVO> children = new ArrayList<>();
}
