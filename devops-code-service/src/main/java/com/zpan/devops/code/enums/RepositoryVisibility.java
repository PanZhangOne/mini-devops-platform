package com.zpan.devops.code.enums;

import lombok.Getter;

@Getter
public enum RepositoryVisibility {
    PRIVATE("私有"),
    PUBLIC("公开"),
    INTERNAL("内部");

    private final String description;

    RepositoryVisibility(String description) {
        this.description = description;
    }

    public static boolean isValid(String value) {
        for (RepositoryVisibility repositoryVisibility : RepositoryVisibility.values()) {
            if (repositoryVisibility.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
