package com.zpan.devops.release.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.release.model.request.VersionCreateRequest;
import com.zpan.devops.release.model.request.VersionStatusUpdateRequest;
import com.zpan.devops.release.model.request.VersionUpdateRequest;
import com.zpan.devops.release.model.vo.VersionVO;
import com.zpan.devops.release.service.VersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @PostMapping
    public Result<VersionVO> create(
            @Valid @RequestBody VersionCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(versionService.create(request, currentUserId));
    }

    @GetMapping
    public Result<List<VersionVO>> list(@RequestParam(required = false) Long projectId) {
        return Result.success(versionService.list(projectId));
    }

    @GetMapping("/{id}")
    public Result<VersionVO> getById(@PathVariable Long id) {
        return Result.success(versionService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<VersionVO> update(
            @PathVariable Long id,
            @Valid @RequestBody VersionUpdateRequest request
    ) {
        return Result.success(versionService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<VersionVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody VersionStatusUpdateRequest request
    ) {
        return Result.success(versionService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        versionService.delete(id);
        return Result.success();
    }
}
