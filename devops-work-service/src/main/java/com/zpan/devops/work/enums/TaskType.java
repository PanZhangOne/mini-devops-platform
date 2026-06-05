package com.zpan.devops.work.enums;

import lombok.Getter;

@Getter
public enum TaskType {
    REQUIREMENT("需求"),
    TASK("任务"),
    BUG("缺陷"),
    STORY("用户故事"),
    SUB_TASK("子任务");

    private final String description;

    TaskType(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (TaskType item : values()) {
            if (item.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
