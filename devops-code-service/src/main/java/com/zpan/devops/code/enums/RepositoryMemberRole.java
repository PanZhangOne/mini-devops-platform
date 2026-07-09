package com.zpan.devops.code.enums;

import lombok.Getter;

@Getter
public enum RepositoryMemberRole {
    OWNER("所有者"),
    MAINTAINER("维护者"),
    DEVELOPER("开发者"),
    REPORTER("报告者"),
    GUEST("访客");

    private final String description;

    RepositoryMemberRole(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        for (RepositoryMemberRole repositoryMemberRole : RepositoryMemberRole.values()) {
            if (repositoryMemberRole.getDescription().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
