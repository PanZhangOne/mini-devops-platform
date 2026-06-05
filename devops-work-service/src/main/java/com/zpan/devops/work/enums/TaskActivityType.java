package com.zpan.devops.work.enums;

import lombok.Getter;

@Getter
public enum TaskActivityType {
    CREATE_TASK("创建任务"),
    UPDATE_TASK("更新任务"),
    CHANGE_STATUS("变更任务状态"),
    CHANGE_PRIORITY("变更任务优先级"),
    CHANGE_ASSIGNEE("变更负责人"),
    ADD_COMMENT("添加评论"),
    CREATE_SUB_TASK("创建子任务"),
    LINK_BRANCH("关联分支"),
    LINK_COMMIT("关联提交"),
    LINK_MERGE_REQUEST("关联合并请求"),
    LINK_PIPELINE_RUN("关联流水线运行");

    private final String description;

    TaskActivityType(String description) {

        this.description = description;

    }
}
