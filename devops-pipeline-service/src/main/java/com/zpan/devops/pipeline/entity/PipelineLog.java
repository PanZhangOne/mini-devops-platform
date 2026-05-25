package com.zpan.devops.pipeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_pipeline_log")
public class PipelineLog {

    @TableId(type = IdType.AUTO)

    private Long id;

    private Long pipelineRunId;

    private Long stepRunId;

    private LocalDateTime logTime;

    private String logLevel;

    private String content;

    private LocalDateTime createdAt;
}
