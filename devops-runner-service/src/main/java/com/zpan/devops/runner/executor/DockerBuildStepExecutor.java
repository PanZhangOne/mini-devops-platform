package com.zpan.devops.runner.executor;

import com.zpan.devops.runner.model.ExecuteResult;
import com.zpan.devops.runner.model.LogConsumer;
import com.zpan.devops.runner.model.PipelineStepRunVO;
import com.zpan.devops.runner.model.RunnerTaskVO;
import com.zpan.devops.runner.model.step.DockerBuildStepConfig;
import com.zpan.devops.runner.util.StepConfigParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class DockerBuildStepExecutor {

    private final StepConfigParser stepConfigParser;

    private final CommandProcessExecutor commandProcessExecutor;

    public ExecuteResult execute(RunnerTaskVO task, PipelineStepRunVO stepRun, Path workspaceDir, LogConsumer logConsumer) {
        DockerBuildStepConfig config;

        try {
            config = stepConfigParser.parse(stepRun.getConfigJson(), DockerBuildStepConfig.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String contextDir = config.getContextDir();
        String dockerfile = config.getDockerfile();
        String imageTag = config.getImageTag();

        if (contextDir == null || contextDir.isBlank()) {
            contextDir = "source";
        }
        if (dockerfile == null || dockerfile.isBlank()) {
            dockerfile = "Dockerfile";
        }

        if (imageTag == null || imageTag.isBlank()) {
            imageTag = task.getImageTag();
        }
        if (imageTag == null || imageTag.isBlank()) {
            return ExecuteResult.failed(-1, "镜像标签不能为空，请在运行流水线时传入 imageTag 或者在步骤配置中指定 imageTag");
        }

        Path contextPath = workspaceDir.resolve(contextDir).normalize();
        if (!contextPath.startsWith(workspaceDir.normalize())) {
            return ExecuteResult.failed(-1, "Docker build 上下文目录非法: " + contextDir);
        }
        if (!Files.exists(contextPath)) {
            return ExecuteResult.failed(-1, "Docker build 上下文目录不存在");
        }

        Path dockerfilePath = contextPath.resolve(dockerfile);
        if (!dockerfilePath.startsWith(contextPath.normalize())) {
            return ExecuteResult.failed(-1, "Dockerfile 路径非法");
        }
        if (!Files.exists(dockerfilePath)) {
            return ExecuteResult.failed(-1, "Dockerfile 不存在");
        }

        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("docker-build ");
        commandBuilder.append("-t ").append(shellQuote(imageTag)).append(" ");
        commandBuilder.append("-f ").append(shellQuote(dockerfile)).append(" ");
        if (config.getBuildArgs() != null && !config.getBuildArgs().isEmpty()) {
            for (var entry : config.getBuildArgs().entrySet()) {
                commandBuilder.append("--build-arg ").append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
            }
        }
        commandBuilder.append(".");
        String command = commandBuilder.toString();
        ExecuteResult result = commandProcessExecutor.execute(command, contextPath, logConsumer);

        return ExecuteResult.of(result.isSuccess(), result.getExitCode(), result.getErrorMessage());
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
