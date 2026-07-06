package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_task_relation")
public class TaskRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String relationType;

    private Long relationId;

    private String relationKey;

    private String relationTitle;

    private Long createdBy;

    private LocalDateTime createdAt;
}
