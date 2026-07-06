package com.zpan.devops.work.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskCommentCreateRequest {
    /**
     * 父评论 ID。
     * 为空表示一级评论。
     */
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
