package com.zpan.devops.runner.model.step;

import lombok.Data;

@Data
public class MavenBuildStepConfig {

    private String workDir;

    private String goals;
}
