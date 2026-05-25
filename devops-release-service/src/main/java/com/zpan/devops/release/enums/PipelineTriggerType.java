package com.zpan.devops.release.enums;

import lombok.Getter;

@Getter
public enum PipelineTriggerType {

    MANUAL("手动触发"),
    WEBHOOK("Webhook 触发"),
    SCHEDULED("定时触发");

    private final String description;

    PipelineTriggerType(String description) {
        this.description = description;
    }

    public static boolean isValid(String triggerType) {
        if (triggerType == null || triggerType.isBlank()) {
            return false;
        }

        for (PipelineTriggerType item : values()) {
            if (item.name().equals(triggerType)) {
                return true;
            }
        }

        return false;
    }
}
