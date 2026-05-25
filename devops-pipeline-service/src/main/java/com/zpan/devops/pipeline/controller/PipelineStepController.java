package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.model.request.PipelineStepCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineStepUpdateRequest;
import com.zpan.devops.pipeline.model.vo.PipelineStepVO;
import com.zpan.devops.pipeline.service.PipelineStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PipelineStepController {
    private final PipelineStepService pipelineStepService;

    @PostMapping("/pipelines/{pipelineId}/steps")
    public Result<PipelineStepVO> create(
            @PathVariable Long pipelineId,
            @Valid @RequestBody PipelineStepCreateRequest request
    ) {
        return Result.success(pipelineStepService.create(pipelineId, request));
    }

    @GetMapping("/pipelines/{pipelineId}/steps")
    public Result<List<PipelineStepVO>> listByPipelineId(@PathVariable Long pipelineId) {
        return Result.success(pipelineStepService.listByPipelineId(pipelineId));
    }

    @GetMapping("/pipeline-steps/{id}")
    public Result<PipelineStepVO> getById(@PathVariable Long id) {
        return Result.success(pipelineStepService.getById(id));
    }

    @PutMapping("/pipeline-steps/{id}")
    public Result<PipelineStepVO> update(
            @PathVariable Long id,
            @Valid @RequestBody PipelineStepUpdateRequest request
    ) {
        return Result.success(pipelineStepService.update(id, request));
    }

    @DeleteMapping("/pipeline-steps/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        pipelineStepService.delete(id);
        return Result.success();
    }
}
