package com.zpan.devops.code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_code_repository")
public class CodeRepository {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String namespace;

    private String name;

    private String path;

    private String description;

    private String defaultBranch;

    private String visibility;

    private String repositoryPath;

    private String cloneHttpUrl;

    private String status;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}