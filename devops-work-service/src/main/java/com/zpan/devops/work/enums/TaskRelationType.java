package com.zpan.devops.work.enums;

import lombok.Getter;

@Getter
public enum TaskRelationType {
    BRANCH("分支"),
    COMMIT("提交"),
    MERGE_REQUEST("合并请求"),
    PIPELINE_RUN("流水线运行"),
    VERSION("版本"),
    RELEASE("发布");

    /**
     * 说明文字
     */
    private String description;

    TaskRelationType(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        for (TaskRelationType type : values()) {
            if (type.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String getDescription(String value) {
        for (TaskRelationType type : values()) {
            if (type.name().equals(value)) {
                return type.description;
            }
        }
        return null;
    }

}
