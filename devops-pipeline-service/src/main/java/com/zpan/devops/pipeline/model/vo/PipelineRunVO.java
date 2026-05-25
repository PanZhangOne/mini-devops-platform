package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PipelineRunVO {
    private Long id;

    private Long pipelineId;

    private Long projectId;

    private Long repositoryId;

    private Long versionId;

    private String runNo;

    private String branchName;

    private String commitHash;

    private String imageTag;

    private String env;

    private String status;

    private String statusDescription;

    private Long triggerUserId;

    private String triggerType;

    private String triggerTypeDescription;

    private String assignedRunnerName;

    private String workspaceDir;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long durationSeconds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
