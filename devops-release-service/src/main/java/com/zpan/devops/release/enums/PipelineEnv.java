package com.zpan.devops.release.enums;

import lombok.Getter;

@Getter
public enum PipelineEnv {

    DEV("开发环境"),
    TEST("测试环境"),
    STAGING("预发环境"),
    PROD("生产环境");

    private final String description;

    PipelineEnv(String description) {
        this.description = description;
    }

    public static boolean isValid(String env) {
        if (env == null || env.isBlank()) {
            return false;
        }

        for (PipelineEnv item : values()) {
            if (item.name().equals(env)) {
                return true;
            }
        }

        return false;
    }
}
