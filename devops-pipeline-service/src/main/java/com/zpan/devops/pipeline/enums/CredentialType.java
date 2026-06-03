package com.zpan.devops.pipeline.enums;

import lombok.Getter;

@Getter
public enum CredentialType {

    USERNAME_PASSWORD("用户名密码"),
    TOKEN("Token");

    private final String description;

    CredentialType(String description) {
        this.description = description;
    }

    public static boolean isValid(String type) {
        for (CredentialType credentialType : CredentialType.values()) {
            if (credentialType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}