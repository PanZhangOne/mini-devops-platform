package com.zpan.devops.work.enums;

import lombok.Getter;

@Getter
public enum ProjectStatus {

    PLANNING("规划中"),
    DEVELOPING("开发中"),
    TESTING("测试中"),
    RELEASED("已发布"),
    ARCHIVED("已归档");

    private final String description;


    ProjectStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        for (ProjectStatus value : values()) {
            if (value.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
}
