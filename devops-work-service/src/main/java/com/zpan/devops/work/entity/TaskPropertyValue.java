package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_task_property_value")
public class TaskPropertyValue {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long propertyId;

    private String propertyCode;

    private String valueText;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
