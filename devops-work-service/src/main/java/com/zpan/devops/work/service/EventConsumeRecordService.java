package com.zpan.devops.work.service;

import com.zpan.devops.work.event.TaskEvent;

public interface EventConsumeRecordService {

    boolean alreadyConsume(String eventId, String consumerGroup);

    void markConsumed(TaskEvent event, String consumerGroup);

    void markFailed(TaskEvent event, String consumerGroup, Exception exception);
}
