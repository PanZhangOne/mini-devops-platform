package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("devops_task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long moduleId;

    private Long parentTaskId;

    private String taskNo;

    private String title;

    private String description;

    private Long assigneeId;

    private Long reporterId;

    private String taskType;

    private String status;

    private String priority;

    private LocalDateTime deadline;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private BigDecimal estimatedHours;

    private BigDecimal actualHours;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
