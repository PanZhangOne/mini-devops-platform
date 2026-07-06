package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_task_comment")
public class TaskComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long parentId;

    private String content;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
