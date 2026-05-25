package com.zpan.devops.runner.executor;

import com.zpan.devops.runner.config.RunnerProperties;
import com.zpan.devops.runner.model.ExecuteResult;
import com.zpan.devops.runner.model.LogConsumer;
import com.zpan.devops.runner.model.PipelineStepRunVO;
import com.zpan.devops.runner.model.RunnerTaskVO;
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

    private final CommandProcessExecutor commandProcessExecutor;

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

        String registry = runnerProperties.getHarborRegistry();
        String username = runnerProperties.getHarborUsername();
        String password = runnerProperties.getHarborPassword();

        if (registry == null || registry.isBlank()) {
            return ExecuteResult.failed(-1, "镜像仓库地址不能为空");
        }
        if (username == null || username.isBlank()) {
            return ExecuteResult.failed(-1, "镜像仓库用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return ExecuteResult.failed(-1, "镜像仓库密码不能为空");
        }

        String loginCommand = "echo " + shellQuote(password)
                + " | docker login " + shellQuote(registry)
                + " -u " + shellQuote(username)
                + " --password-stdin";

        ExecuteResult loginResult = commandProcessExecutor.execute(
                loginCommand,
                workspaceDir,
                logConsumer
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

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
