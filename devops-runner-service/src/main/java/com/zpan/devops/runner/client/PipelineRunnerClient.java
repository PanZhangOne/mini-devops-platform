package com.zpan.devops.runner.client;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.runner.model.*;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.zpan.devops.runner.model.RunnerTaskWorkspaceUpdateRequest;

@FeignClient(name = "devops-pipeline-service")
public interface PipelineRunnerClient {

    @PostMapping("/internal/runners/register")
    Result<PipelineRunnerVO> register(@Valid @RequestBody PipelineRunnerRegisterRequest request);

    @PostMapping("/internal/runners/heartbeat")
    Result<PipelineRunnerVO> heartbeat(@Valid @RequestBody PipelineRunnerHeartbeatRequest request);

    @PostMapping("/internal/runner-tasks/next")
    Result<RunnerTaskVO> fetchNext(@Valid @RequestBody RunnerTaskNextRequest request);

    @PostMapping("/internal/runner-tasks/{pipelineRunId}/workspace")
    Result<Void> updateWorkspace(
            @PathVariable("pipelineRunId") Long pipelineRunId,
            @Valid @RequestBody RunnerTaskWorkspaceUpdateRequest request
    );

    @PostMapping("/internal/runner-tasks/{pipelineRunId}/steps/{stepRunId}/start")
    Result<Void> startStep(
            @PathVariable("pipelineRunId") Long pipelineRunId,
            @PathVariable("stepRunId") Long stepRunId,
            @Valid @RequestBody RunnerTaskStepStartRequest request
    );

    @PostMapping("/internal/runner-tasks/{pipelineRunId}/steps/{stepRunId}/finish")
    Result<Void> finishStep(
            @PathVariable("pipelineRunId") Long pipelineRunId,
            @PathVariable("stepRunId") Long stepRunId,
            @Valid @RequestBody RunnerTaskStepFinishRequest request
    );

    @PostMapping("/internal/runner-tasks/{pipelineRunId}/logs")
    Result<Void> appendLog(
            @PathVariable("pipelineRunId") Long pipelineRunId,
            @Valid @RequestBody RunnerTaskLogAppendRequest request
    );

    @PostMapping("/internal/runner-tasks/{pipelineRunId}/finish")
    Result<Void> finishRun(
            @PathVariable("pipelineRunId") Long pipelineRunId,
            @Valid @RequestBody RunnerTaskFinishRequest request
    );
}
