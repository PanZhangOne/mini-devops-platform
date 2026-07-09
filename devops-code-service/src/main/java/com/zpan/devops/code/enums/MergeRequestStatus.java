package com.zpan.devops.code.enums;

import lombok.Getter;

@Getter
public enum MergeRequestStatus {

    OPEN("开启"),
    MERGED("已合并"),
    CLOSED("已关闭"),
    CONFLICT("存在冲突");

    private final String description;

    MergeRequestStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (MergeRequestStatus item : values()) {
            if (item.name().equals(value)) {
                return true;
            }
        }

        return false;
    }
}