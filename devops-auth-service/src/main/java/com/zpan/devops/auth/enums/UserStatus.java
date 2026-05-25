package com.zpan.devops.auth.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ENABLED("启用"),
    DISABLED("禁用");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
