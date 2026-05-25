package com.zpan.devops.code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_repository")
public class Repository {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String repoName;

    private String repoUrl;

    private String repoType;

    private String defaultBranch;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
