package com.zpan.devops.code.enums;

import lombok.Getter;

@Getter
public enum RepoType {

    GITLAB("GitLab"),
    GITHUB("GitHub"),
    GITEE("Gitee"),
    CUSTOM("自定义 Git 仓库");


    private final String description;

    RepoType(String description) {
        this.description = description;
    }

    public static boolean isValid(String repoType) {
        if (repoType == null || repoType.isBlank()) {
            return false;
        }

        for (RepoType item : values()) {
            if (item.name().equalsIgnoreCase(repoType)) {
                return true;
            }
        }

        return false;
    }
}
