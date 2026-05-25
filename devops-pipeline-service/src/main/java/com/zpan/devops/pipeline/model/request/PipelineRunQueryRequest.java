package com.zpan.devops.pipeline.model.request;

import lombok.Data;

@Data
public class PipelineRunQueryRequest {

    private Long pipelineId;

    private Long projectId;

    private Long repositoryId;

    private Long versionId;

    private String status;

    private String triggerType;
}
