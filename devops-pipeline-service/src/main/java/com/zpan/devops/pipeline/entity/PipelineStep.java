package com.zpan.devops.pipeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_pipeline_step")
public class PipelineStep {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pipelineId;

    private String name;

    private String stepType;

    private Integer sortOrder;

    private String command;

    private String configJson;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
