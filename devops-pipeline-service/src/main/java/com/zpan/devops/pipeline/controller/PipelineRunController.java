package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.model.request.PipelineRunCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineRunQueryRequest;
import com.zpan.devops.pipeline.model.vo.*;
import com.zpan.devops.pipeline.service.PipelineRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PipelineRunController {
    private final PipelineRunService pipelineRunService;

    @PostMapping("/pipelines/{pipelineId}/runs")

    public Result<PipelineRunVO> create(
            @PathVariable Long pipelineId,
            @Valid @RequestBody PipelineRunCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(pipelineRunService.create(pipelineId, request, currentUserId));
    }

    @GetMapping("/pipeline-runs")
    public Result<List<PipelineRunVO>> list(PipelineRunQueryRequest query) {
        return Result.success(pipelineRunService.list(query));
    }

    @GetMapping("/pipeline-runs/{id}")
    public Result<PipelineRunVO> getById(@PathVariable Long id) {
        return Result.success(pipelineRunService.getById(id));
    }

    @GetMapping("/pipeline-runs/{id}/steps")
    public Result<List<PipelineStepRunVO>> listSteps(@PathVariable Long id) {
        return Result.success(pipelineRunService.listSteps(id));
    }

    @GetMapping("/pipeline-runs/{id}/logs")
    public Result<List<PipelineLogVO>> listLogs(@PathVariable Long id) {
        return Result.success(pipelineRunService.listLogs(id));
    }
}
