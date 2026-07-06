package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.request.TaskRelationCreateRequest;
import com.zpan.devops.work.model.vo.TaskRelationVO;
import com.zpan.devops.work.service.TaskRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskRelationController {

    private final TaskRelationService taskRelationService;

    @PostMapping("/tasks/{taskId}/relations")
    public Result<TaskRelationVO> create(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRelationCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(taskRelationService.create(taskId, request, currentUserId));
    }

    @GetMapping("/tasks/{taskId}/relations")
    public Result<List<TaskRelationVO>> listByTaskId(@PathVariable Long taskId) {
        return Result.success(taskRelationService.listByTaskId(taskId));
    }

    @DeleteMapping("/task-relations/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        taskRelationService.delete(id, currentUserId);
        return Result.success();
    }
}
