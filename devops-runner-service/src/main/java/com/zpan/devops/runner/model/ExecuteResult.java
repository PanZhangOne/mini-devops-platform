package com.zpan.devops.runner.model;

import lombok.Data;

@Data
public class ExecuteResult {
    private boolean success;

    private Integer exitCode;

    private String errorMessage;

    public static ExecuteResult success(Integer exitCode) {
        ExecuteResult result = new ExecuteResult();
        result.success = true;
        result.exitCode = exitCode;
        return result;
    }

    public static ExecuteResult failed(Integer exitCode, String errorMessage) {
        ExecuteResult result = new ExecuteResult();
        result.success = false;
        result.errorMessage = errorMessage;
        result.exitCode = exitCode;
        return result;
    }


    public static  ExecuteResult of(boolean success, Integer exitCode, String errorMessage) {
        ExecuteResult result = new ExecuteResult();
        result.success = success;
        result.exitCode = exitCode;
        result.errorMessage = errorMessage;
        return result;
    }
}
