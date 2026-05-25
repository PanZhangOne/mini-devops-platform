package com.zpan.devops.runner.executor;

import com.zpan.devops.runner.model.ExecuteResult;
import com.zpan.devops.runner.model.LogConsumer;
import com.zpan.devops.runner.model.PipelineStepRunVO;
import com.zpan.devops.runner.model.step.GitCloneStepConfig;
import com.zpan.devops.runner.util.StepConfigParser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitCloneStepExecutor {

    private final StepConfigParser stepConfigParser;

    public ExecuteResult execute(PipelineStepRunVO stepRun, Path workspaceDir, LogConsumer logConsumer) {
        GitCloneStepConfig config;

        try {
            config = stepConfigParser.parse(stepRun.getConfigJson(), GitCloneStepConfig.class);
        } catch (Exception e) {
            return ExecuteResult.failed(-1, e.getMessage());
        }

        String repoUrl = config.getRepoUrl();
        String branchName = config.getBranchName();
        String targetDir = config.getTargetDir();

        if (repoUrl == null || repoUrl.isBlank()) {
            return ExecuteResult.failed(-1, "Git 仓库地址不能为空");
        }
        if (branchName == null || branchName.isBlank()) {
            branchName = "main";
        }
        if (targetDir == null || targetDir.isBlank()) {
            targetDir = "source";
        }

        String command = "git clone --branch " + shellQuote(branchName) + " " + shellQuote(repoUrl) + " " + shellQuote(targetDir);

        return executeCommand(command, workspaceDir, logConsumer);
    }

    private ExecuteResult executeCommand(String command, Path workspaceDir, LogConsumer logConsumer) {
        Process process = null;

        try {
            logConsumer.accept("INFO", "工作目录: " + workspaceDir.toAbsolutePath());
            logConsumer.accept("INFO", "执行命令: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.directory(workspaceDir.toFile());
            processBuilder.redirectErrorStream(true);

            process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsumer.accept("INFO", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ExecuteResult.success(exitCode);
            }

            return ExecuteResult.failed(exitCode, "Git clone 失败, exitCode=" + exitCode);
        } catch (Exception e) {
            log.warn("Git clone step execute failed, stepRunId={}", null, e);
            return ExecuteResult.failed(-1, e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }
}
