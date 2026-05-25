package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

@Data
public class PipelineRunCompleteEventVO {

    private Long pipelineRunId;

    private String status;

    private String message;
}
