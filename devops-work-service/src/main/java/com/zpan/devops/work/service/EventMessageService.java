package com.zpan.devops.work.service;

import com.zpan.devops.work.event.TaskEvent;

public interface EventMessageService {
    void saveTaskEvent(TaskEvent event);

    void publishPendingMessage();
}
