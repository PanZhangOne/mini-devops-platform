package com.zpan.devops.release.model.request;

import lombok.Data;

@Data
public class PipelineRunQueryRequest {

    private Long projectId;

    private Long repositoryId;

    private Long versionId;

    private String env;

    private String status;
}
