package com.zpan.devops.work.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ProjectModuleTreeVO {


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

    private List<ProjectModuleTreeVO> children = new ArrayList<>();
}
