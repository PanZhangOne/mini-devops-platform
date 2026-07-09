package com.zpan.devops.code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_code_branch")
public class CodeBranch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repositoryId;

    private String branchName;

    private String lastCommitHash;

    private Boolean protectedBranch;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}