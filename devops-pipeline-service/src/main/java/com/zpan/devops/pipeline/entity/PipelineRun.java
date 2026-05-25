package com.zpan.devops.pipeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_pipeline_run")
public class PipelineRun {

    @TableId(type = IdType.AUTO)
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

    private Long triggerUserId;

    private String triggerType;

    private String assignedRunnerName;

    private String workspaceDir;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long durationSeconds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}