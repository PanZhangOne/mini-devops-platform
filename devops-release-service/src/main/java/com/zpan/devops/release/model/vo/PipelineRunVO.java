package com.zpan.devops.release.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PipelineRunVO {

    private Long id;

    private Long projectId;

    private Long repositoryId;

    private Long versionId;

    private String runNo;

    private String env;

    private String envDescription;

    private String status;

    private String statusDescription;

    private String imageTag;

    private String commitHash;

    private Long triggerUserId;

    private String triggerType;

    private String triggerTypeDescription;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long durationSeconds;

    private String logText;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
