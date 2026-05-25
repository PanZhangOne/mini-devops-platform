package com.zpan.devops.runner.service;

import com.zpan.devops.runner.config.RunnerProperties;
import com.zpan.devops.runner.model.RunnerTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkspaceManager {

    private final RunnerProperties runnerProperties;

    public Path prepareWorkspace(RunnerTaskVO task) {
        String workspaceRoot = runnerProperties.getWorkspaceRoot();

        if (workspaceRoot == null || workspaceRoot.isBlank()) {
            workspaceRoot = "/tmp/mini-devops-runner/workspaces";
        }
        Path workspacePath = Path.of(workspaceRoot, task.getRunNo());

        try {
            Files.createDirectories(workspacePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return workspacePath;
    }
}
