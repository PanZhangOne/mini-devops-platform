package com.zpan.devops.code.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeRepositoryVO {

    private Long id;

    private Long projectId;

    private String namespace;

    private String name;

    private String path;

    private String description;

    private String defaultBranch;

    private String visibility;

    private String visibilityDescription;

    private String repositoryPath;

    private String cloneHttpUrl;

    private String status;

    private String statusDescription;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}