package com.zpan.devops.work.event;

import com.zpan.devops.work.entity.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class TaskEventFactory {

    private static final String SOURCE_SERVICE =  "devops-work-service";

    public TaskEvent statusChanged(
            Task task,
            String oldStatus,
            String newStatus,
            String remark,
            Long operatorId
    ) {
        TaskEvent event = new TaskEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(TaskEventType.TASK_STATUS_CHANGED.name());
        event.setSourceService(SOURCE_SERVICE);
        event.setProjectId(task.getProjectId());
        event.setTaskId(task.getId());
        event.setTaskNo(task.getTaskNo());
        event.setOperatorId(operatorId);
        event.setOccurredAt(LocalDateTime.now());

        event.setData(Map.of(
                "oldStatus", oldStatus,
                "newStatus", newStatus,
                "remark", remark == null ? "" : remark
        ));

        return event;
    }
}
