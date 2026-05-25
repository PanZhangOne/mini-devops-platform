package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RunnerVO {
    private Long id;

    private String runnerName;

    private String ip;

    private Integer port;

    private String status;

    private String statusDescription;

    private Integer maxConcurrency;

    private Integer currentConcurrency;

    private LocalDateTime lastHeartbeatAt;

    private LocalDateTime registeredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
