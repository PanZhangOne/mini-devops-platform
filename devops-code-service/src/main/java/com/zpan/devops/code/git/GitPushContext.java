package com.zpan.devops.code.git;

import lombok.Data;

@Data
public class GitPushContext {

    private Long repositoryId;

    private String namespace;

    private String repositoryPath;

    private Long pusherId;
}
