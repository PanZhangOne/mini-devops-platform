package com.zpan.devops.code.enums;

import lombok.Getter;

@Getter
public enum RepositoryStatus {

    ACTIVE("正常"),
    ARCHIVED("归档"),
    DISABLED("禁用");

    private final String description;

    RepositoryStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        for (RepositoryStatus status : RepositoryStatus.values()) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
