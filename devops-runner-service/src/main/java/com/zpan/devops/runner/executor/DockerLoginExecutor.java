package com.zpan.devops.runner.executor;

import com.zpan.devops.runner.model.ExecuteResult;
import com.zpan.devops.runner.model.LogConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Slf4j
@Component
public class DockerLoginExecutor {

    public ExecuteResult login(String registry, String username, String password, Path workDir, LogConsumer logConsumer) {
        Process process = null;

        try {
            logConsumer.accept("INFO", "执行命令: docker login " + registry + " -u " + username + " --password-stdin");

            ProcessBuilder processBuilder = new ProcessBuilder("docker", "login", registry, "-u", username, "--password-stdin");
            processBuilder.directory(workDir.toFile());
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();


            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(password);
                writer.write(System.lineSeparator());
                writer.flush();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsumer.accept("INFO", maskSensitive(line, password));
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ExecuteResult.success(exitCode);
            }
            return ExecuteResult.failed(exitCode, "Docker login 失败, exitCode=" + exitCode);
        } catch (Exception e) {
            logConsumer.accept("ERROR", "执行命令失败: " + e.getMessage());
            return ExecuteResult.failed(-1, e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    private String maskSensitive(String content, String secret) {
        if (content == null || secret == null || secret.isBlank()) {
            return content;
        }
        return content.replace(secret, "******");
    }
}
