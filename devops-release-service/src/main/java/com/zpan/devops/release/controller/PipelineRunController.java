package com.zpan.devops.release.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.release.model.request.PipelineRunCreateRequest;
import com.zpan.devops.release.model.request.PipelineRunQueryRequest;
import com.zpan.devops.release.model.request.PipelineRunStatusUpdateRequest;
import com.zpan.devops.release.model.vo.PipelineRunVO;
import com.zpan.devops.release.service.PipelineRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pipeline-runs")
public class PipelineRunController {


    private final PipelineRunService pipelineRunService;

    @PostMapping
    public Result<PipelineRunVO> create(
            @Valid @RequestBody PipelineRunCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(pipelineRunService.create(request, currentUserId));
    }

    @GetMapping
    public Result<List<PipelineRunVO>> list(PipelineRunQueryRequest query) {
        return Result.success(pipelineRunService.list(query));
    }

    @GetMapping("/{id}")
    public Result<PipelineRunVO> getById(@PathVariable("id") Long id) {
        return Result.success(pipelineRunService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public Result<PipelineRunVO> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody PipelineRunStatusUpdateRequest request
    ) {
        return Result.success(pipelineRunService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        return Result.success();
    }
}
