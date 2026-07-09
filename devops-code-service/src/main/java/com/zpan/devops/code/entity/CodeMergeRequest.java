package com.zpan.devops.code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_code_merge_request")
public class CodeMergeRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repositoryId;

    private Long projectId;

    private String sourceBranch;

    private String targetBranch;

    private String title;

    private String description;

    private String status;

    private String mergeCommitHash;

    private Long createdBy;

    private Long mergedBy;

    private Long closedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime mergedAt;

    private LocalDateTime closedAt;
}