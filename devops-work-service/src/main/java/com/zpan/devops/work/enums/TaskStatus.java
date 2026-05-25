package com.zpan.devops.work.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {

    TODO("待处理"), IN_PROGRESS("进行中"), TESTING("测试中"), DONE("已完成"), CANCELLED("已取消");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }

        for (TaskStatus item : values()) {
            if (item.name().equals(status)) {
                return true;
            }
        }

        return false;
    }
}
