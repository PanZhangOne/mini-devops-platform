package com.zpan.devops.runner.executor;

import com.zpan.devops.runner.model.ExecuteResult;
import com.zpan.devops.runner.model.LogConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Component
@Slf4j
public class CommandProcessExecutor {

    public ExecuteResult execute(String command, Path workDir, LogConsumer logConsumer) {
        Process process = null;

        try {
            logConsumer.accept("INFO", "工作目录: " + workDir.toAbsolutePath());
            logConsumer.accept("INFO", "执行命令: " + maskSensitive(command));

            ProcessBuilder builder = new ProcessBuilder("sh", "-c", command);
            builder.redirectErrorStream(true);
            builder.directory(workDir.toFile());

            process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsumer.accept("INFO", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ExecuteResult.success(exitCode);
            }

            return ExecuteResult.failed(exitCode, "命令执行失败，exitCode=" + exitCode);
        } catch (IOException | InterruptedException e) {
            log.warn("Command execute failed, command={}", command, e);
            return ExecuteResult.failed(-1, e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    private String maskSensitive(String command) {
        if (command == null) {
            return null;
        }
        return command.replaceAll("(?i)(--password\\s+)[^\\s]+", "$1******");
    }
}
