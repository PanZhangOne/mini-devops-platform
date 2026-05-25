package com.zpan.devops.code.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepositoryVO {
    private Long id;

    private Long projectId;

    private String repoName;

    private String repoUrl;

    private String defaultBranch;

    private String repoType;

    private String repoTypeDescription;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
