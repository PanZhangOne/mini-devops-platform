package com.zpan.devops.runner.executor;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.runner.client.PipelineRunnerClient;
import com.zpan.devops.runner.config.RunnerProperties;
import com.zpan.devops.runner.model.*;
import com.zpan.devops.runner.model.step.DockerPushStepConfig;
import com.zpan.devops.runner.util.StepConfigParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class DockerPushStepExecutor {

    private final RunnerProperties runnerProperties;

    private final StepConfigParser stepConfigParser;

    private final DockerLoginExecutor dockerLoginExecutor;

    private final CommandProcessExecutor commandProcessExecutor;

    private final PipelineRunnerClient pipelineRunnerClient;

    public ExecuteResult execute(RunnerTaskVO task, PipelineStepRunVO stepRun, Path workspaceDir, LogConsumer logConsumer) {
        DockerPushStepConfig config;

        try {
            config = stepConfigParser.parse(stepRun.getConfigJson(), DockerPushStepConfig.class);
        } catch (Exception e) {
            return ExecuteResult.failed(-1, e.getMessage());
        }

        String imageTag = config.getImageTag();
        if (imageTag == null || imageTag.isBlank()) {
            imageTag = task.getImageTag();
        }
        if (imageTag == null || imageTag.isBlank()) {
            return ExecuteResult.failed(-2, "镜像标签不能为空");
        }

        Long credentialId = config.getCredentialId();
        if (credentialId == null || credentialId <= 0) {
            return ExecuteResult.failed(-3, "凭证ID不能为空");
        }

        String registry = runnerProperties.getHarborRegistry();
        if (registry == null || registry.isBlank()) {
            return ExecuteResult.failed(-1, "镜像仓库地址不能为空");
        }
        CredentialSecretVO credential = fetchCredentialSecret(credentialId);
        if (!"USERNAME_PASSWORD".equals(credential.getCredentialType())) {
            return ExecuteResult.failed(-1, "凭证类型必须是USERNAME_PASSWORD");
        }
        if (credential.getUsername() == null || credential.getUsername().isBlank()) {
            return ExecuteResult.failed(-1, "Harbor 用户名不能为空");
        }
        if (credential.getSecretValue() == null || credential.getSecretValue().isBlank()) {
            return ExecuteResult.failed(-1, "Harbor 密码不能为空");
        }


        ExecuteResult loginResult = dockerLoginExecutor.login(
                registry,
                credential.getUsername(),
                credential.getSecretValue(),
                workspaceDir,
                logConsumer::accept
        );

        if (!loginResult.isSuccess()) {
            return ExecuteResult.of(false, loginResult.getExitCode(), "Docker login失败：" + loginResult.getErrorMessage());
        }

        String pushCommand = "docker push " + shellQuote(imageTag);
        ExecuteResult pushResult = commandProcessExecutor.execute(
                pushCommand,
                workspaceDir,
                logConsumer::accept
        );

        return ExecuteResult.of(pushResult.isSuccess(), pushResult.getExitCode(), pushResult.getErrorMessage());
    }

    private CredentialSecretVO fetchCredentialSecret(Long credentialId) {
        Result<CredentialSecretVO> result = pipelineRunnerClient.getCredentialSecret(credentialId);

        if (result == null || result.getCode() == null || result.getCode() != 0) {
            String message = result == null ? "获取凭据失败" : result.getMessage();
            throw new IllegalStateException(message);
        }
        return result.getData();
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
