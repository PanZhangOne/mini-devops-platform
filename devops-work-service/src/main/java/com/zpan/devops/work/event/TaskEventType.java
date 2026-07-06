package com.zpan.devops.work.event;

import lombok.Getter;

@Getter
public enum TaskEventType {
    TASK_CREATED("任务已创建"),
    TASK_UPDATED("任务已更新"),
    TASK_STATUS_CHANGED("任务状态已变更"),
    TASK_COMMENT_ADDED("任务评论已添加"),
    TASK_COMMENT_DELETED("任务评论已删除");

    private final String description;

    TaskEventType(String description) {
        this.description = description;
    }
}
