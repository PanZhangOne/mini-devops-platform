package com.zpan.devops.pipeline.enums;

import lombok.Getter;

@Getter
public enum RunnerStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    BUSY("忙碌"),
    DISABLED("禁用");

    private final String description;

    RunnerStatus(String description) {
        this.description = description;
    }

    public static boolean isValid(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }

        for (RunnerStatus item : values()) {
            if (item.name().equals(status)) {
                return true;
            }
        }

        return false;
    }
}
