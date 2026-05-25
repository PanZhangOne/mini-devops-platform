package com.zpan.devops.pipeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_runner")
public class Runner {
    @TableId(type = IdType.AUTO)

    private Long id;

    private String runnerName;

    private String runnerToken;

    private String ip;

    private Integer port;

    private String status;

    private Integer maxConcurrency;

    private Integer currentConcurrency;

    private LocalDateTime lastHeartbeatAt;

    private LocalDateTime registeredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
