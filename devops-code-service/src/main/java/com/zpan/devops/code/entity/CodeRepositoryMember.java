package com.zpan.devops.code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_code_repository_member")
public class CodeRepositoryMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repositoryId;

    private Long userId;

    private String roleCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}