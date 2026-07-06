package com.zpan.devops.work.event;

import lombok.Getter;

@Getter
public enum EventMessageStatus {

    NEW("待发送"),
    SENT("已发送"),
    FAILED("发送失败"),
    DEAD("超过最大重试次数");

    private final String description;

    EventMessageStatus(String description) {
        this.description = description;
    }
}
