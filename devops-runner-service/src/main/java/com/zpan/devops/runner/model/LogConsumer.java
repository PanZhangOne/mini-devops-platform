package com.zpan.devops.runner.model;

@FunctionalInterface
public interface LogConsumer {
    void accept(String logLevel, String content);
}
