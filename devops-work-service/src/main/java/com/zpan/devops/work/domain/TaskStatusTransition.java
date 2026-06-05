package com.zpan.devops.work.domain;

import com.zpan.devops.work.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TaskStatusTransition {

    private final Map<String, Set<String>> allowedTransitions = Map.of(
            TaskStatus.TODO.name(), Set.of(TaskStatus.IN_PROGRESS.name(), TaskStatus.CANCELLED.name()),
            TaskStatus.IN_PROGRESS.name(), Set.of(TaskStatus.TESTING.name(), TaskStatus.CANCELLED.name()),
            TaskStatus.TESTING.name(), Set.of(TaskStatus.DONE.name(), TaskStatus.IN_PROGRESS.name(), TaskStatus.CANCELLED.name()),
            TaskStatus.DONE.name(), Set.of(TaskStatus.CANCELLED.name())
    );


    public boolean canTransit(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return  false;
        }

        Set<String> allowedTransition = allowedTransitions.get(currentStatus);
        return allowedTransition.contains(targetStatus);
    }
}
