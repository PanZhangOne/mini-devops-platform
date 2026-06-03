package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CredentialVO {

    private Long id;

    private Long projectId;

    private String name;

    private String credentialType;

    private String credentialTypeDescription;

    private String username;

    private String description;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
