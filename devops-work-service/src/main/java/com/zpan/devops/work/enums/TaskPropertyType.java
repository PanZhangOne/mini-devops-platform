package com.zpan.devops.work.enums;

import lombok.Getter;

@Getter
public enum TaskPropertyType {

    TEXT("文本"),
    NUMBER("数字"),
    DATE("日期"),
    SELECT("单选"),
    MULTI_SELECT("多选"),
    USER("用户"),
    BOOLEAN("布尔值");

    private final String description;

    TaskPropertyType(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (TaskPropertyType item : values()) {
            if (item.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
