package com.zpan.devops.pipeline.enums;

import lombok.Getter;

@Getter
public enum PipelineRunStatus {

    PENDING("等待中"),
    RUNNING("运行中"),
    SUCCESS("成功"),
    FAILED("失败"),
    CANCELLED("已取消"),
    TIMEOUT("超时");

    private final String description;

    PipelineRunStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (PipelineRunStatus item : values()) {
            if (item.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFinished(String status) {
        return SUCCESS.name().equals(status)
                || FAILED.name().equals(status)
                || CANCELLED.name().equals(status)
                || TIMEOUT.name().equals(status);
    }
}
