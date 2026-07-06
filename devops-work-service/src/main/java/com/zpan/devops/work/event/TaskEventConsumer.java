package com.zpan.devops.work.event;

import com.zpan.devops.work.config.DevopsMqProperties;
import com.zpan.devops.work.model.request.TaskPropertyValueSaveItemRequest;
import com.zpan.devops.work.service.EventConsumeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${devops.mq.topic.task-event}",
        consumerGroup = "${devops.mq.consumer.task-event-group}",
        messageModel = MessageModel.CLUSTERING
)
public class TaskEventConsumer implements RocketMQListener<TaskEvent> {

    private final EventConsumeRecordService eventConsumeRecordService;

    private final DevopsMqProperties devopsMqProperties;


    @Override
    public void onMessage(TaskEvent event) {

        String consumerGroup = devopsMqProperties.getConsumer().getTaskEventGroup();

        if (eventConsumeRecordService.alreadyConsume(event.getEventId(), consumerGroup)) {
            log.info(
                    "Task event already consumed, skip. eventId={}, consumerGroup={}",
                    event.getEventId(), consumerGroup
            );
        }

        try {
            log.info(
                    "Task event consumed. eventId={}, eventType={}, taskId={}, taskNo={}, data={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getTaskId(),
                    event.getTaskNo(),
                    event.getData()
            );

            if (TaskEventType.TASK_STATUS_CHANGED.name().equals(event.getEventType())) {
                handleTaskStatusChanged(event);
            }

            eventConsumeRecordService.markConsumed(event, consumerGroup);
        } catch (Exception e) {
            eventConsumeRecordService.markFailed(event, consumerGroup, e);
        }

    }

    private void handleTaskStatusChanged(TaskEvent event) {
        Object oldStatus = event.getData() == null ? null : event.getData().get("oldStatus");
        Object newStatus = event.getData() == null ? null : event.getData().get("newStatus");

        log.info(
                "Handle TASK_STATUS_CHANGED. taskId={}, oldStatus={}, newStatus={}",
                event.getTaskId(),
                oldStatus,
                newStatus
        );
    }
}
