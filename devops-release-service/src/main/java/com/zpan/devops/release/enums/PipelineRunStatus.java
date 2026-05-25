package com.zpan.devops.release.enums;

import lombok.Getter;

@Getter
public enum PipelineRunStatus {

    PENDING("等待中"),
    RUNNING("运行中"),
    SUCCESS("成功"),
    FAILED("失败"),
    CANCELLED("已取消");

    private final String description;

    PipelineRunStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }

        for (PipelineRunStatus item : values()) {
            if (item.name().equals(status)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFinished(String status) {
        return SUCCESS.name().equals(status)
                || FAILED.name().equals(status)
                || CANCELLED.name().equals(status);
    }
}
