package com.zpan.devops.code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_code_commit")
public class CodeCommit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repositoryId;

    private String branchName;

    private String commitHash;

    private String shortHash;

    private String commitMessage;

    private String authorName;

    private String authorEmail;

    private LocalDateTime committedAt;

    private LocalDateTime createdAt;
}