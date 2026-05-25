package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PipelineLogVO {

    private Long id;

    private Long pipelineRunId;

    private Long stepRunId;

    private LocalDateTime logTime;

    private String logLevel;

    private String content;

    private LocalDateTime createdAt;
}
