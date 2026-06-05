package com.zpan.devops.work.model.request;

import com.zpan.devops.common.request.PagedRequest;
import lombok.Data;

@Data
public class TaskListRequest extends PagedRequest {

    private Long moduleId;

    private Long parentTaskId;

    private String status;

    private String taskType;

    private String priority;

    private Long assigneeId;
}
