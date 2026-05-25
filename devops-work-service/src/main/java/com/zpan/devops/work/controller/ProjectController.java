package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.request.ProjectCreateRequest;
import com.zpan.devops.work.model.request.ProjectUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectTaskStatsVO;
import com.zpan.devops.work.model.vo.ProjectVO;
import com.zpan.devops.work.model.vo.TaskVO;
import com.zpan.devops.work.service.ProjectService;
import com.zpan.devops.work.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    private final TaskService taskService;

    @PostMapping
    public Result<ProjectVO> create(@Valid @RequestBody ProjectCreateRequest request) {
        return Result.success(projectService.create(request));
    }

    @GetMapping
    public Result<List<ProjectVO>> list() {
        return Result.success(projectService.list());
    }

    @GetMapping("/{id}")
    public Result<ProjectVO> getById(@PathVariable("id") Long id) {
        return Result.success(projectService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<ProjectVO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return Result.success(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        projectService.delete(id);
        return Result.success();
    }

    @GetMapping("/{projectId}/tasks")
    public Result<List<TaskVO>> listTasks(@PathVariable("projectId") Long projectId) {
        return Result.success(taskService.listByProjectId(projectId));
    }

    @GetMapping("/{projectId}/task-stats")
    public Result<ProjectTaskStatsVO> getTaskStats(@PathVariable("projectId") Long projectId) {
        return Result.success(taskService.getProjectTaskStats(projectId));
    }
}
