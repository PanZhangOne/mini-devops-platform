package com.zpan.devops.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // devops-common 项目
    SUCCESS(0, "success"),
    PARAM_ERROR(10000, "请求参数错误"),
    UNAUTHORIZED(10001, "未登录或登录已过期"),
    FORBIDDEN(10002, "无权限访问"),
    NOT_FOUND(10003, "资源不存在"),
    SYSTEM_ERROR(10004, "系统内部异常"),
    SERVICE_UNAVAILABLE(10005, "服务暂不可用"),
    REMOTE_SERVICE_ERROR(10006, "远程服务调用失败"),

    // devops-auth-service
    USERNAME_EXISTS(20001, "用户名已存在"),
    USERNAME_OR_PASSWORD_ERROR(20002, "用户名或密码错误"),
    USER_DISABLED(20003, "用户已被禁用"),
    TOKEN_INVALID(20004, "Token 无效"),
    TOKEN_EXPIRED(20005, "Token 已过期"),

    // devops-work-service
    PROJECT_NOT_FOUND(30001, "项目不存在"),
    PROJECT_CODE_EXISTS(30002, "项目编码已存在"),
    PROJECT_HAS_TASKS(30003, "项目中存在任务"),
    TASK_NOT_FOUND(30004, "任务不存在"),
    TASK_STATUS_INVALID(30005, "任务状态不合法"),
    PROJECT_MODULE_PARENT_INVALID(30006, "父模块不合法"),
    PROJECT_MODULE_HAS_CHILDREN(30007, "模块下存在子模块"),
    PROJECT_MODULE_HAS_TASKS(30008, "模块下存在任务"),
    PROJECT_MODULE_NOT_FOUND(30009, "模块不存在"),
    TASK_STATUS_TRANSITION_INVALID(30010, "任务状态转换不合法"),

    // devops-code-service
    REPOSITORY_NOT_FOUND(40001, "代码仓库不存在"),
    REPOSITORY_URL_EXISTS(40002, "该项目下代码仓库地址已存在"),

    // devops-release-service
    VERSION_NOT_FOUND(50001, "版本不存在"),
    VERSION_NO_EXISTS(50002, "该项目下版本号已存在"),
    VERSION_STATUS_INVALID(50003, "版本状态不合法"),

    // devops-pipeline-service
    PIPELINE_NOT_FOUND(70001, "流水线不存在"),
    PIPELINE_DISABLED(700011, "流水线已禁用"),
    PIPELINE_STEP_EMPTY(70012, "流水线步骤为空"),
    PIPELINE_CODE_EXISTS(70002, "该项目下流水线编码已存在"),
    PIPELINE_TRIGGER_TYPE_INVALID(70003, "流水线触发类型不合法"),
    PIPELINE_STEP_NOT_FOUND(71004, "流水线步骤不存在"),
    PIPELINE_STEP_TYPE_INVALID(71005, "流水线步骤类型不合法"),
    PIPELINE_RUN_NOT_FOUND(72006, "流水线运行记录不存在"),
    PIPELINE_RUN_STATUS_INVALID(72007, "流水线运行状态不合法"),
    PIPELINE_RUN_ENV_INVALID(73010, "流水线环境不合法"),
    PIPELINE_STEP_ORDER_EXISTS(73020,"流水线步骤顺序已存在"),

    // devops-runner-service
    RUNNER_NOT_FOUND(74001, "Runner 不存在"),
    RUNNER_TOKEN_INVALID(74002, "Runner Token无效"),
    RUNNER_DISABLED(74003, "Runner已禁用"),
    RUNNER_STATUS_INVALID(74004, "Runner状态不合法"),

    CREDENTIAL_NOT_FOUND(75001, "凭据不存在"),
    CREDENTIAL_TYPE_INVALID(75002, "凭据类型不合法"),
    CREDENTIAL_NAME_EXISTS(75003, "凭据名称已存在"),
    CREDENTIAL_SECRET_REQUIRED(75004, "凭据密钥不能为空");


    private final Integer code;

    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
