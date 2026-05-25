package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVO {
    private Long id;

    private String name;

    private String code;

    private String description;

    private Long ownerId;

    private String status;

    private String statusDescription;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
