package com.zpan.devops.pipeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_pipeline_step_run")
public class PipelineStepRun {
    @TableId(type = IdType.AUTO)

    private Long id;

    private Long pipelineRunId;

    private Long pipelineStepId;

    private String name;

    private String stepType;

    private Integer sortOrder;

    private String command;

    private String configJson;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long durationSeconds;

    private Integer exitCode;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
