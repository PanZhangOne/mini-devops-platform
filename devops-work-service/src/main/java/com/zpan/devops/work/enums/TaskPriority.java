package com.zpan.devops.work.enums;

import lombok.Getter;

@Getter
public enum TaskPriority {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    URGENT("紧急");

    private final String description;

    TaskPriority(String description) {
        this.description = description;
    }

    public static boolean isValid(String priority) {
        if (priority == null || priority.isBlank()) {
            return false;
        }

        for (TaskPriority item : values()) {
            if (item.name().equals(priority)) {
                return true;
            }
        }

        return false;
    }
}
