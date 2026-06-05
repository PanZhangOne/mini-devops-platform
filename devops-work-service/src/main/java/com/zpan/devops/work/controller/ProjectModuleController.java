package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.request.ProjectModuleCreateRequest;
import com.zpan.devops.work.model.request.ProjectModuleUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectModuleTreeVO;
import com.zpan.devops.work.model.vo.ProjectModuleVO;
import com.zpan.devops.work.service.ProjectModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectModuleController {

    private final ProjectModuleService projectModuleService;

    @PostMapping("/projects/{projectId}/modules")
    public Result<ProjectModuleVO> create(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectModuleCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId

    ) {
        return Result.success(projectModuleService.create(projectId, request, currentUserId));
    }

    @GetMapping("/projects/{projectId}/modules")
    public Result<List<ProjectModuleVO>> listByProjectId(@PathVariable Long projectId) {
        return Result.success(projectModuleService.listByProjectId(projectId));
    }

    @GetMapping("/projects/{projectId}/modules/tree")
    public Result<List<ProjectModuleTreeVO>> treeByProjectId(@PathVariable Long projectId) {
        return Result.success(projectModuleService.treeByProjectId(projectId));
    }

    @GetMapping("/project-modules/{id}")
    public Result<ProjectModuleVO> getById(@PathVariable Long id) {
        return Result.success(projectModuleService.getById(id));
    }

    @PutMapping("/project-modules/{id}")
    public Result<ProjectModuleVO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectModuleUpdateRequest request
    ) {
        return Result.success(projectModuleService.update(id, request));
    }

    @DeleteMapping("/project-modules/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectModuleService.delete(id);
        return Result.success();
    }
}
