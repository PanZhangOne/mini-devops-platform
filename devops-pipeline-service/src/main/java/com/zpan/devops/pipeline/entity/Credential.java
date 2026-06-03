package com.zpan.devops.pipeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_credential")
public class Credential {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String name;

    private String credentialType;

    private String username;

    private String secretValue;

    private String description;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
