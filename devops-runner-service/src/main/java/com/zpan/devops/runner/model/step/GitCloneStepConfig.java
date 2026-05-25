package com.zpan.devops.runner.model.step;

import lombok.Data;

@Data
public class GitCloneStepConfig {
    private String repoUrl;

    private String branchName;

    /**
     * 克隆到workspace的那个目录下
     */
    private String targetDir;
}
