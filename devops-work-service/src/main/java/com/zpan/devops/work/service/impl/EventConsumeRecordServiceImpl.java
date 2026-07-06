package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.work.entity.EventConsumeRecord;
import com.zpan.devops.work.event.EventConsumeStatus;
import com.zpan.devops.work.event.TaskEvent;
import com.zpan.devops.work.mapper.EventConsumeRecordMapper;
import com.zpan.devops.work.service.EventConsumeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventConsumeRecordServiceImpl implements EventConsumeRecordService {

    private final EventConsumeRecordMapper eventConsumeRecordMapper;

    @Override
    public boolean alreadyConsume(String eventId, String consumerGroup) {
        LambdaQueryWrapper<EventConsumeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventConsumeRecord::getEventId, eventId);
        wrapper.eq(EventConsumeRecord::getConsumerGroup, consumerGroup);
        wrapper.eq(EventConsumeRecord::getStatus, EventConsumeStatus.SUCCESS.name());

        return eventConsumeRecordMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConsumed(TaskEvent event, String consumerGroup) {
        LocalDateTime now = LocalDateTime.now();

        EventConsumeRecord record = findRecord(event.getEventId(), consumerGroup);
        if (record == null) {
            record = new EventConsumeRecord();
            record.setEventId(event.getEventId());
            record.setConsumerGroup(consumerGroup);
            record.setEventType(event.getEventType());
            record.setStatus(EventConsumeStatus.SUCCESS.name());
            record.setErrorMessage(null);
            record.setConsumedAt(now);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            eventConsumeRecordMapper.insert(record);
            return;
        }
        record.setEventType(event.getEventType());
        record.setStatus(EventConsumeStatus.SUCCESS.name());
        record.setErrorMessage(null);
        record.setConsumedAt(now);
        record.setUpdatedAt(now);
        eventConsumeRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(TaskEvent event, String consumerGroup, Exception exception) {
        LocalDateTime now = LocalDateTime.now();

        EventConsumeRecord record = findRecord(event.getEventId(), consumerGroup);

        if (record == null) {
            record = new EventConsumeRecord();
            record.setEventId(event.getEventId());
            record.setConsumerGroup(consumerGroup);
            record.setEventType(event.getEventType());
            record.setStatus(EventConsumeStatus.FAILED.name());
            record.setErrorMessage(abbreviate(exception.getMessage(), 100));
            record.setConsumedAt(now);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            eventConsumeRecordMapper.insert(record);
            return;
        }

        record.setEventType(event.getEventType());
        record.setStatus(EventConsumeStatus.FAILED.name());
        record.setErrorMessage(abbreviate(exception.getMessage(), 100));
        record.setConsumedAt(now);
        record.setUpdatedAt(now);
        eventConsumeRecordMapper.updateById(record);
    }

    private EventConsumeRecord findRecord(String eventId, String consumerGroup) {
        LambdaQueryWrapper<EventConsumeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventConsumeRecord::getEventId, eventId);
        wrapper.eq(EventConsumeRecord::getConsumerGroup, consumerGroup);
        return eventConsumeRecordMapper.selectOne(wrapper);
    }

    private String abbreviate(String text, int length) {
        if (text == null) {
            return null;
        }
        if (text.length() <= length) {
            return text;
        }
        return text.substring(0, length) + "...";
    }
}
