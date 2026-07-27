package com.zpan.devops.code.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeBranchVO {

    private Long id;

    private Long repositoryId;

    private String branchName;

    private String lastCommitHash;

    private String lastCommitShortHash;

    private Boolean protectedBranch;

    private Boolean defaultBranch;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
