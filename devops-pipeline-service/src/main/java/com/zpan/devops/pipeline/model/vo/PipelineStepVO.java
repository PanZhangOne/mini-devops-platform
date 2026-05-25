package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PipelineStepVO {
    private Long id;

    private Long pipelineId;

    private String name;

    private String stepType;

    private String stepTypeDescription;

    private Integer sortOrder;

    private String command;

    private String configJson;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
