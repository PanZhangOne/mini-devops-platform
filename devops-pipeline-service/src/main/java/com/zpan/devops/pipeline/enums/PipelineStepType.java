package com.zpan.devops.pipeline.enums;

import lombok.Getter;

@Getter
public enum PipelineStepType {

    SHELL("Shell命令"),
    GIT_CLONE("Git拉取代码"),
    MAVEN_BUILD("Maven构建"),
    DOCKER_BUILD("Docker构建"),
    DOCKER_PUSH("Docker推送"),
    DOCKER_DEPLOY("Docker部署"),
    HTTP_CHECK("HTTP健康检查");

    private final String description;

    PipelineStepType(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (PipelineStepType item : values()) {
            if (item.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
