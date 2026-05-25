package com.zpan.devops.release.enums;

import lombok.Getter;

@Getter
public enum VersionStatus {
    DRAFT("草稿"),
    READY("待发布"),
    RELEASED("已发布"),
    ROLLBACKED("已回滚"),
    CANCELLED("已取消");

    private final String description;

    VersionStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }

        for (VersionStatus item : values()) {
            if (item.name().equals(status)) {
                return true;
            }
        }

        return false;
    }
}
