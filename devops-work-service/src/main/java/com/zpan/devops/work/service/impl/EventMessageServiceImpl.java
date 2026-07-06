package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpan.devops.work.config.DevopsMqProperties;
import com.zpan.devops.work.entity.EventMessage;
import com.zpan.devops.work.event.EventMessageStatus;
import com.zpan.devops.work.event.TaskEvent;
import com.zpan.devops.work.mapper.EventMessageMapper;
import com.zpan.devops.work.service.EventMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventMessageServiceImpl implements EventMessageService {

    private static final int DEFAULT_MAX_RETRY_COUNT = 5;

    private static final int BATCH_SIZE = 20;

    private final EventMessageMapper eventMessageMapper;

    private final ObjectMapper objectMapper;

    private final RocketMQTemplate rocketMQTemplate;

    private final DevopsMqProperties devopsMqProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTaskEvent(TaskEvent event) {
        LocalDateTime now = LocalDateTime.now();

        EventMessage message = new EventMessage();
        message.setEventId(event.getEventId());
        message.setTopic(devopsMqProperties.getTopic().getTaskEvent());
        message.setEventType(event.getEventType());
        message.setEventKey(event.getTaskNo());
        message.setSourceService(event.getSourceService());
        message.setPayloadJson(toJson(event));
        message.setStatus(EventMessageStatus.NEW.name());
        message.setRetryCount(0);
        message.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        message.setNextRetryAt(now);
        message.setSentAt(null);
        message.setErrorMessage(null);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        eventMessageMapper.insert(message);



        log.info(
                "Local event message saved. eventId={}, eventType={}, topic={}",
                event.getEventId(),
                event.getEventType(),
                message.getTopic()
        );

    }

    @Override
    public void publishPendingMessage() {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<EventMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(EventMessage::getStatus, EventMessageStatus.NEW.name(), EventMessageStatus.FAILED.name());
        queryWrapper.le(EventMessage::getNextRetryAt, now);
        queryWrapper.orderByAsc(EventMessage::getCreatedAt);
        queryWrapper.last("LIMIT " + BATCH_SIZE);

        List<EventMessage> messages = eventMessageMapper.selectList(queryWrapper);

        if (messages.isEmpty()) {
            return;
        }

        for (EventMessage message : messages) {
            publishOne(message);
        }
    }

    private void publishOne(EventMessage message) {
        try {
            TaskEvent event = objectMapper.readValue(message.getPayloadJson(), TaskEvent.class);
            rocketMQTemplate.convertAndSend(message.getTopic(), event);
            markSent(message);


            log.info(
                    "Local event message saved. eventId={}, eventType={}, topic={}",
                    event.getEventId(),
                    event.getEventType(),
                    message.getTopic()
            );
        } catch (Exception e) {
            markFailed(message, e);

            log.warn(
                    "Local event message send failed. id={}, eventId={}, retryCount={}",
                    message.getId(),
                    message.getEventId(),
                    message.getRetryCount(),
                    e
            );
        }
    }

    private void markSent(EventMessage message) {
        LocalDateTime now = LocalDateTime.now();

        EventMessage update = new EventMessage();
        update.setId(message.getId());
        update.setStatus(EventMessageStatus.SENT.name());
        update.setSentAt(now);
        update.setErrorMessage(null);
        update.setUpdatedAt(now);

        eventMessageMapper.updateById(update);
    }

    private void markFailed(EventMessage message, Exception e) {
        LocalDateTime now = LocalDateTime.now();
        int nextRetryCount = message.getRetryCount() + 1;

        EventMessage update = new EventMessage();
        update.setId(message.getId());
        update.setStatus(EventMessageStatus.FAILED.name());
        update.setErrorMessage(abbreviate(e.getMessage(), 100));
        update.setUpdatedAt(now);

        if (nextRetryCount >= message.getMaxRetryCount()) {
            update.setStatus(EventMessageStatus.DEAD.name());
            update.setNextRetryAt(null);
        } else {
            update.setStatus(EventMessageStatus.FAILED.name());
            update.setNextRetryAt(now.plusSeconds(calculateRetryDelaySeconds(nextRetryCount)));
        }

        eventMessageMapper.updateById(update);
    }

    private long calculateRetryDelaySeconds(int retryCount) {
        return switch (retryCount) {
            case 1 -> 10;
            case 2 -> 30;
            case 3 -> 60;
            case 4 -> 120;
            default -> 300;
        };
    }

    private String toJson(TaskEvent event) {

        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Serialize task event failed", e);
        }
    }

    private String abbreviate(String value, int maxLength) {

        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
