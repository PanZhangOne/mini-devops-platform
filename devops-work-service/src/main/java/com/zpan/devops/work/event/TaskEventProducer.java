package com.zpan.devops.work.event;

import com.zpan.devops.work.config.DevopsMqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventProducer {

    private final RocketMQTemplate rocketMQTemplate;

    private final DevopsMqProperties devopsMqProperties;

    public void send(TaskEvent event) {
        String topic = devopsMqProperties.getTopic().getTaskEvent();
        rocketMQTemplate.convertAndSend(topic, event);

        log.info(
                "Task event sent. topic={}, eventId={}, eventType={}, taskId={}",
                topic,
                event.getEventId(),
                event.getEventType(),
                event.getTaskId()
        );
    }

}
