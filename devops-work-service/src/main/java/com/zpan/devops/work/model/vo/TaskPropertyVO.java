package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskPropertyVO {
    private Long id;

    private Long projectId;

    private String name;

    private String code;

    private String propertyType;

    private String propertyTypeDescription;

    private Boolean required;

    private String optionsJson;

    private Integer sortOrder;

    private Boolean enabled;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

