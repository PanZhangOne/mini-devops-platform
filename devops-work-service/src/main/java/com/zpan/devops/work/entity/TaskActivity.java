package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_task_activity")
public class TaskActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String actionType;

    private String actionContent;

    private String oldValue;

    private String newValue;

    private Long createdBy;

    private LocalDateTime createdAt;
}
