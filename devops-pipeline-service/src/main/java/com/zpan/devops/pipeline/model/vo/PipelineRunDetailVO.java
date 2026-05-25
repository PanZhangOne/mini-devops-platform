package com.zpan.devops.pipeline.model.vo;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PipelineRunDetailVO {
    private Long id;

    private Long pipelineId;

    private String pipelineName;

    private Long projectId;

    private Long repositoryId;

    private String triggerType;

    private String triggerTypeDescription;

    private String status;

    private String statusDescription;

    private Long triggerBy;

    private String triggerByName;

    private String branch;

    private String commitId;

    private String remark;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;
}
