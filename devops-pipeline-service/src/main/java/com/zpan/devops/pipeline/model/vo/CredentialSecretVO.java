package com.zpan.devops.pipeline.model.vo;

import lombok.Data;

@Data
public class CredentialSecretVO {
    private Long id;

    private Long projectId;

    private String name;

    private String credentialType;

    private String username;

    private String secretValue;
}
