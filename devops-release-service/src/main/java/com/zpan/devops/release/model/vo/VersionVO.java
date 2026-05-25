package com.zpan.devops.release.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VersionVO {

    private Long id;

    private Long projectId;

    private Long repositoryId;

    private String versionNo;

    private String gitTag;

    private String branchName;

    private String commitHash;

    private String title;

    private String description;

    private String status;

    private String statusDescription;

    private Long createdBy;

    private LocalDateTime releasedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
