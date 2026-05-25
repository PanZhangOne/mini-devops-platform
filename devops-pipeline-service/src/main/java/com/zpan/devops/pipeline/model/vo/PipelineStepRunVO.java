package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PipelineStepRunVO {
    private Long id;

    private Long pipelineRunId;

    private Long pipelineStepId;

    private String name;

    private String stepType;

    private String stepTypeDescription;

    private Integer sortOrder;

    private String command;

    private String configJson;

    private String status;

    private String statusDescription;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long durationSeconds;

    private Integer exitCode;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
