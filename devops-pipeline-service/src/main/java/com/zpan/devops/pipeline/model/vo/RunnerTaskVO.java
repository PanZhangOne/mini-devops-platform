package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class RunnerTaskVO {
    private Long pipelineRunId;

    private String runNo;

    private Long pipelineId;

    private Long projectId;

    private Long repositoryId;

    private Long versionId;

    private String branchName;

    private String commitHash;

    private String imageTag;

    private String env;

    private String assignedRunnerName;

    private String workspaceDir;

    private List<PipelineStepRunVO> steps;
}
