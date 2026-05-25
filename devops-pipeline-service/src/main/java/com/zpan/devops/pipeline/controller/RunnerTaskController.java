package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.model.request.*;
import com.zpan.devops.pipeline.model.vo.RunnerTaskVO;
import com.zpan.devops.pipeline.service.RunnerTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/runner-tasks")
public class RunnerTaskController {
    private final RunnerTaskService runnerTaskService;

    @PostMapping("/next")
    public Result<RunnerTaskVO> fetchNext(@Valid @RequestBody RunnerTaskNextRequest request) {
        return Result.success(runnerTaskService.fetchNext(request));
    }

    @PostMapping("/{pipelineRunId}/workspace")
    public Result<Void> updateWorkspace(
            @PathVariable Long pipelineRunId,
            @Valid @RequestBody RunnerTaskWorkspaceUpdateRequest request
    ) {
        runnerTaskService.updateWorkspace(pipelineRunId, request);
        return Result.success();
    }

    @PostMapping("/{pipelineRunId}/steps/{stepRunId}/start")
    public Result<Void> startStep(
            @PathVariable Long pipelineRunId,
            @PathVariable Long stepRunId,
            @Valid @RequestBody RunnerTaskStepStartRequest request
    ) {
        runnerTaskService.startStep(pipelineRunId, stepRunId, request);
        return Result.success();
    }

    @PostMapping("/{pipelineRunId}/steps/{stepRunId}/finish")
    public Result<Void> finishStep(
            @PathVariable Long pipelineRunId,
            @PathVariable Long stepRunId,
            @Valid @RequestBody RunnerTaskStepFinishRequest request
    ) {
        runnerTaskService.finishStep(pipelineRunId, stepRunId, request);
        return Result.success();
    }

    @PostMapping("/{pipelineRunId}/logs")
    public Result<Void> appendLog(
            @PathVariable Long pipelineRunId,
            @Valid @RequestBody RunnerTaskLogAppendRequest request
    ) {
        runnerTaskService.appendLog(pipelineRunId, request);
        return Result.success();
    }

    @PostMapping("/{pipelineRunId}/finish")
    public Result<Void> finishRun(
            @PathVariable Long pipelineRunId,
            @Valid @RequestBody RunnerTaskFinishRequest request
    ) {
        runnerTaskService.finishRun(pipelineRunId, request);
        return Result.success();
    }
}
