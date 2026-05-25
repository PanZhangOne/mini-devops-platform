package com.zpan.devops.runner.service;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.runner.client.PipelineRunnerClient;
import com.zpan.devops.runner.config.RunnerProperties;
import com.zpan.devops.runner.executor.*;
import com.zpan.devops.runner.model.*;
import com.zpan.devops.runner.runtime.RunnerRuntimeState;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunnerTaskPoller {

    private final RunnerProperties runnerProperties;

    private final PipelineRunnerClient pipelineRunnerClient;

    private final RunnerRuntimeState runnerRuntimeState;

    private final ShellStepExecutor shellStepExecutor;

    private final MavenBuildStepExecutor mavenBuildStepExecutor;

    private final GitCloneStepExecutor gitCloneStepExecutor;

    private final DockerBuildStepExecutor dockerBuildStepExecutor;

    private final DockerPushStepExecutor dockerPushStepExecutor;

    private final WorkspaceManager workspaceManager;

    @Scheduled(fixedDelayString = "${runner.task-poll-interval-ms:5000}")
    public void poll() {
        if (runnerRuntimeState.getCurrentConcurrency() >= runnerProperties.getMaxConcurrency()) {
            return;
        }
        RunnerTaskNextRequest request = new RunnerTaskNextRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());

        Result<RunnerTaskVO> result;

        try {
            result = pipelineRunnerClient.fetchNext(request);
        } catch (Exception e) {
            log.warn("Fetch runner task failed", e);
            return;
        }
        if (result == null || result.getCode() == null || result.getCode() != 0) {
            log.warn("Fetch runner task response invalid, message={}", result == null ? null : result.getMessage());
            return;
        }

        RunnerTaskVO task = result.getData();
        if (task == null) {
            return;
        }
        runnerRuntimeState.increment();
        try {
            executeTask(task);
        } finally {
            runnerRuntimeState.decrement();
        }
    }

    private void executeTask(RunnerTaskVO task) {
        Path workspacePath;

        try {
            workspacePath = workspaceManager.prepareWorkspace(task);
        } catch (Exception e) {
            String errorMsg = "准备工作目录失败： " + e.getMessage();
            appendRunLog(task.getPipelineRunId(), null, "ERROR", errorMsg);
            finishRun(task.getPipelineRunId(), "FAILED", errorMsg);
            return;
        }

        appendRunLog(task.getPipelineRunId(), null, "INFO", "Runner 开始执行流水线：" + task.getRunNo());

        boolean success = true;
        String errorMessage = null;

        for (PipelineStepRunVO step : task.getSteps()) {
            startStep(task.getPipelineRunId(), step.getId());

            StepExecuteResult executeResult = executeStep(task, step, workspacePath);

            if (executeResult.isSuccess()) {
                finishStep(task.getPipelineRunId(), step.getId(), "SUCCESS", executeResult.getExitCode(), null);
            } else {
                success = false;
                errorMessage = executeResult.getErrorMessage();
                finishStep(task.getPipelineRunId(), step.getId(), "FAILED", executeResult.getExitCode(), errorMessage);
                break;
            }
        }

        if (success) {
            finishRun(task.getPipelineRunId(), "SUCCESS", null);
        } else {
            finishRun(task.getPipelineRunId(), "FAILED", errorMessage);
        }
    }

    private StepExecuteResult executeStep(RunnerTaskVO task, PipelineStepRunVO step, Path workspacePath) {
        if ("SHELL".equals(step.getStepType())) {
            ExecuteResult result = shellStepExecutor.execute(step, workspacePath, (logLevel, content) -> appendRunLog(task.getPipelineRunId(), step.getId(), logLevel, content));
            return StepExecuteResult.of(result.isSuccess(), result.getExitCode(), result.getErrorMessage());
        }

        if ("GIT_CLONE".equals(step.getStepType())) {
            ExecuteResult result = gitCloneStepExecutor.execute(step, workspacePath, (logLevel, content) -> appendRunLog(task.getPipelineRunId(), step.getId(), logLevel, content));
            return StepExecuteResult.of(result.isSuccess(), result.getExitCode(), result.getErrorMessage());
        }

        if ("MAVEN_BUILD".equals(step.getStepType())) {
            ExecuteResult result = mavenBuildStepExecutor.execute(step, workspacePath, (logLevel, content) -> appendRunLog(task.getPipelineRunId(), step.getId(), logLevel, content));
            return StepExecuteResult.of(result.isSuccess(), result.getExitCode(), result.getErrorMessage());
        }

        if ("DOCKER_BUILD".equals(step.getStepType())) {
            ExecuteResult result = dockerBuildStepExecutor.execute(
                    task,
                    step,
                    workspacePath,
                    (logLevel, content) -> appendRunLog(task.getPipelineRunId(), step.getId(), logLevel, content)
            );
            return StepExecuteResult.of(result.isSuccess(), result.getExitCode(), result.getErrorMessage());
        }
        if ("DOCKER_PUSH".equals(step.getStepType())) {
            ExecuteResult result = dockerPushStepExecutor.execute(
                    task,
                    step,
                    workspacePath,
                    (logLevel, content) -> appendRunLog(task.getPipelineRunId(), step.getId(), logLevel, content)
            );
            return StepExecuteResult.of(result.isSuccess(), result.getExitCode(), result.getErrorMessage());
        }

        return StepExecuteResult.of(false, -1, "暂不支持的步骤类型: " + step.getStepType());
    }

    private void updateWorkspace(Long pipelineRunId, String workspacePath) {
        RunnerTaskWorkspaceUpdateRequest request = new RunnerTaskWorkspaceUpdateRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());
        request.setWorkspaceDir(workspacePath);
        pipelineRunnerClient.updateWorkspace(pipelineRunId, request);
    }

    private void startStep(Long pipelineRunId, Long stepRunId) {
        RunnerTaskStepStartRequest request = new RunnerTaskStepStartRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());
        pipelineRunnerClient.startStep(pipelineRunId, stepRunId, request);
    }

    private void finishStep(
            Long pipelineRunId,
            Long stepRunId,
            String status,
            Integer exitCode,
            String errorMessage
    ) {

        com.zpan.devops.runner.model.RunnerTaskStepFinishRequest request = new com.zpan.devops.runner.model.RunnerTaskStepFinishRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());
        request.setStatus(status);
        request.setExitCode(exitCode);
        request.setErrorMessage(errorMessage);

        pipelineRunnerClient.finishStep(pipelineRunId, stepRunId, request);
    }

    private void appendRunLog(Long pipelineRunId, Long stepRunId, String logLevel, String content) {
        RunnerTaskLogAppendRequest request = new RunnerTaskLogAppendRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());
        request.setStepRunId(stepRunId);
        request.setLogLevel(logLevel);
        request.setContent(content);
        pipelineRunnerClient.appendLog(pipelineRunId, request);
    }

    private void finishRun(Long pipelineRunId, String status, String errorMessage) {
        RunnerTaskFinishRequest request = new RunnerTaskFinishRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());
        request.setStatus(status);
        request.setErrorMessage(errorMessage);

        pipelineRunnerClient.finishRun(pipelineRunId, request);
    }

    @Data
    private static class StepExecuteResult {
        private boolean success;
        private Integer exitCode;
        private String errorMessage;

        static StepExecuteResult of(boolean success, Integer exitCode, String errorMessage) {
            StepExecuteResult result = new StepExecuteResult();
            result.setSuccess(success);
            result.setExitCode(exitCode);
            result.setErrorMessage(errorMessage);

            return result;
        }
    }
}
