package com.zpan.devops.work.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectModuleVO {

    private Long id;

    private Long projectId;

    private Long parentId;

    private String name;

    private String code;

    private String description;

    private Integer sortOrder;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
