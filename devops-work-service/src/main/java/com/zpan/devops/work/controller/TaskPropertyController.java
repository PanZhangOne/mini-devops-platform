package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.request.TaskPropertyCreateRequest;
import com.zpan.devops.work.model.request.TaskPropertyUpdateRequest;
import com.zpan.devops.work.model.vo.TaskPropertyVO;
import com.zpan.devops.work.service.TaskPropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskPropertyController {

    private final TaskPropertyService taskPropertyService;

    @PostMapping("/projects/{projectId}/task-properties")
    public Result<TaskPropertyVO> create(

            @PathVariable Long projectId,
            @Valid @RequestBody TaskPropertyCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(taskPropertyService.create(projectId, request, currentUserId));
    }

    @GetMapping("/projects/{projectId}/task-properties")
    public Result<List<TaskPropertyVO>> listByProjectId(@PathVariable Long projectId) {
        return Result.success(taskPropertyService.listByProjectId(projectId));
    }

    @GetMapping("/task-properties/{id}")
    public Result<TaskPropertyVO> getById(@PathVariable Long id) {
        return Result.success(taskPropertyService.getById(id));
    }

    @PutMapping("/task-properties/{id}")
    public Result<TaskPropertyVO> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskPropertyUpdateRequest request
    ) {
        return Result.success(taskPropertyService.update(id, request));
    }

    @DeleteMapping("/task-properties/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskPropertyService.delete(id);
        return Result.success();
    }
}
