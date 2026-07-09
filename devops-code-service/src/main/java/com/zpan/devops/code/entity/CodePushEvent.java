package com.zpan.devops.code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_code_push_event")
public class CodePushEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repositoryId;

    private String branchName;

    private String beforeCommitHash;

    private String afterCommitHash;

    private Long pusherId;

    private Integer commitCount;

    private String eventPayloadJson;

    private LocalDateTime createdAt;
}