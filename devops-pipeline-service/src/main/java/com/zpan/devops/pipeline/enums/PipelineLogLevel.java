package com.zpan.devops.pipeline.enums;

import lombok.Getter;

@Getter
public enum PipelineLogLevel {

    INFO("信息"),
    WARN("警告"),
    ERROR("错误"),
    DEBUG("调试");

    private final String description;

    PipelineLogLevel(String description) {
        this.description = description;
    }
}
