package com.zpan.devops.work.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "devops.mq")
public class DevopsMqProperties {

    private Topic topic = new Topic();

    private Consumer consumer = new Consumer();

    @Data
    public static class Topic {
        private String taskEvent;
    }

    @Data
    public static class Consumer {
        private String taskEventGroup;
    }
}
