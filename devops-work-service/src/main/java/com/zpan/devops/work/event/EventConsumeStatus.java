package com.zpan.devops.work.event;

import lombok.Getter;

@Getter
public enum EventConsumeStatus {

    SUCCESS("消费成功"),
    FAILED("消费失败");

    private final String description;

    EventConsumeStatus(String description) {
        this.description = description;
    }
}
