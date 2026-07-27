package com.zpan.devops.code.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeCommitVO {

    private Long id;

    private Long repositoryId;

    private String branchName;

    private String commitHash;

    private String shortHash;

    private String commitMessage;

    private String commitTitle;

    private String authorName;

    private String authorEmail;

    private LocalDateTime committedAt;

    private LocalDateTime createdAt;
}
