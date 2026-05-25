package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.model.request.RunnerHeartbeatRequest;
import com.zpan.devops.pipeline.model.request.RunnerRegisterRequest;
import com.zpan.devops.pipeline.model.request.RunnerStatusUpdateRequest;
import com.zpan.devops.pipeline.model.vo.RunnerVO;
import com.zpan.devops.pipeline.service.RunnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RunnerController {

    private final RunnerService runnerService;

    @PostMapping("/internal/runners/register")
    public Result<RunnerVO> register(@Valid @RequestBody RunnerRegisterRequest request) {
        return Result.success(runnerService.register(request));
    }

    @PostMapping("/internal/runners/heartbeat")
    public Result<RunnerVO> heartbeat(@Valid @RequestBody RunnerHeartbeatRequest request) {
        return Result.success(runnerService.heartbeat(request));
    }

    @GetMapping("/runners")
    public Result<List<RunnerVO>> list() {
        return Result.success(runnerService.list());
    }

    @GetMapping("/runners/{id}")
    public Result<RunnerVO> getById(@PathVariable Long id) {
        return Result.success(runnerService.getById(id));
    }

    @PatchMapping("/runners/{id}/status")
    public Result<RunnerVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody RunnerStatusUpdateRequest request
    ) {
        return Result.success(runnerService.updateStatus(id, request.getStatus()));
    }
}
