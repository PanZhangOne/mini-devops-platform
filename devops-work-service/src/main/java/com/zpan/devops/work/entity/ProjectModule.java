package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("devops_project_module")
public class ProjectModule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long parentId;

    private String name;

    private String code;

    private String description;

    private Integer sortOrder;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
