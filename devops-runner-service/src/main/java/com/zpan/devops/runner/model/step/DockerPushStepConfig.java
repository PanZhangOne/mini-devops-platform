package com.zpan.devops.runner.model.step;

import lombok.Data;

@Data
public class DockerPushStepConfig {

    private String imageTag;

    private Long credentialId;
}
