package com.zpan.devops.release.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_version")
public class Version {
    @TableId(type = IdType.AUTO)
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

    private Long createdBy;

    private LocalDateTime releasedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
