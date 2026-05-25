package com.zpan.devops.runner.executor;

import com.zpan.devops.runner.model.ExecuteResult;
import com.zpan.devops.runner.model.LogConsumer;
import com.zpan.devops.runner.model.PipelineStepRunVO;
import com.zpan.devops.runner.model.step.MavenBuildStepConfig;
import com.zpan.devops.runner.util.StepConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class MavenBuildStepExecutor {

    private final StepConfigParser stepConfigParser;

    public ExecuteResult execute(PipelineStepRunVO stepRun, Path workspaceDir, LogConsumer logConsumer) {
        MavenBuildStepConfig config;

        try {
            config = stepConfigParser.parse(stepRun.getConfigJson(), MavenBuildStepConfig.class);
        } catch (Exception e) {
            return  ExecuteResult.failed(-1, e.getMessage());
        }

        String workDir = config.getWorkDir();
        String goals = config.getGoals();

        if (workDir == null || workDir.isBlank()) {
            workDir = "source";
        }
        if (goals == null || goals.isBlank()) {
            goals = stepRun.getCommand();
        } else {
            goals = "clean package -DskipTests";
        }

        Path mavenWorkDir = workspaceDir.resolve(workDir).normalize();
        if (!mavenWorkDir.startsWith(workspaceDir.normalize())) {
            return ExecuteResult.failed(-1, "Maven工作目录非法: " + workDir);
        }
        if (!Files.exists(mavenWorkDir)) {
            return ExecuteResult.failed(-1, "Maven工作目录不存在: " + workDir);
        }

        String command = "mvn " + goals;

        return executeCommand(command, mavenWorkDir, logConsumer);
    }

    private ExecuteResult executeCommand(String command, Path workDir, LogConsumer logConsumer) {
        Process process = null;

        try {
            logConsumer.accept("INFO", "Maven工作目录：" + workDir.toAbsolutePath());
            logConsumer.accept("INFO", "执行命令：" + command);

            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.directory(workDir.toFile());
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsumer.accept("INFO", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ExecuteResult.success(exitCode);
            }
            return ExecuteResult.failed(exitCode, "Maven构建失败，exitCode=" + exitCode);
        } catch (Exception e) {
            log.warn("Maven build step execute failed", e);
            return ExecuteResult.failed(-1, e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }
}
