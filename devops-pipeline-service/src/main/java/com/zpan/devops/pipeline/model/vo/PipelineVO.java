package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PipelineVO {
    private Long id;

    private Long projectId;

    private Long repositoryId;

    private String name;

    private String code;

    private String description;

    private String triggerType;

    private String triggerTypeDescription;

    private Boolean enabled;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
