package com.zpan.devops.runner.model.step;

import lombok.Data;

import java.util.Map;

@Data
public class DockerBuildStepConfig {

    private String contextDir;

    private String dockerfile;

    private String imageTag;

    private Map<String, String> buildArgs;
}
