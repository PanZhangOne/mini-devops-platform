package com.zpan.devops.work.model.vo;

import lombok.Data;

@Data
public class ProjectTaskStatsVO {
    private Long projectId;

    private Long totalCount;

    private Long todoCount;

    private Long inProgressCount;

    private Long testingCount;

    private Long doneCount;

    private Long cancelledCount;

    private Integer progress;
}
