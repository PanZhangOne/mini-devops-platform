package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.model.request.PipelineCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineUpdateRequest;
import com.zpan.devops.pipeline.model.vo.PipelineVO;
import com.zpan.devops.pipeline.service.PipelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    @PostMapping
    public Result<PipelineVO> create(
            @Valid @RequestBody PipelineCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(pipelineService.create(request, currentUserId));
    }

    @GetMapping
    public Result<List<PipelineVO>> list(@RequestParam(required = false) Long projectId) {
        return Result.success(pipelineService.list(projectId));
    }

    @GetMapping("/{id}")
    public Result<PipelineVO> getById(@PathVariable Long id) {
        return Result.success(pipelineService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<PipelineVO> update(
            @PathVariable Long id,
            @Valid @RequestBody PipelineUpdateRequest request
    ) {
        return Result.success(pipelineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        pipelineService.delete(id);
        return Result.success();

    }
}
