package com.zpan.devops.runner.executor;

import com.zpan.devops.runner.model.ExecuteResult;
import com.zpan.devops.runner.model.LogConsumer;
import com.zpan.devops.runner.model.PipelineStepRunVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Slf4j
@Component
public class ShellStepExecutor {

    public ExecuteResult execute(PipelineStepRunVO stepRun, Path workspaceDir, LogConsumer logConsumer) {
        String command = stepRun.getCommand();

        if (command == null || command.isBlank()) {
            return ExecuteResult.failed(-1, "Shell 命令不能为空");
        }

        Process process = null;

        try {
            logConsumer.accept("INFO", "工作目录: " + workspaceDir.toAbsolutePath());
            logConsumer.accept("INFO", "执行命令: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.directory(workspaceDir.toFile());
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
            return ExecuteResult.failed(exitCode, "命令执行失败, exitCode=" + exitCode);
        } catch (IOException | InterruptedException e) {
            log.warn("Shell step execute failed, stepRunId={}", stepRun.getId(), e);
            return ExecuteResult.failed(-1, e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

}
