package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.request.TaskCreateRequest;
import com.zpan.devops.work.model.request.TaskStatusUpdateRequest;
import com.zpan.devops.work.model.request.TaskUpdateRequest;
import com.zpan.devops.work.model.vo.TaskVO;
import com.zpan.devops.work.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public Result<TaskVO> create(@Valid @RequestBody TaskCreateRequest request) {
        return Result.success(taskService.create(request));
    }

    @GetMapping
    public Result<List<TaskVO>> list() {
        return Result.success(taskService.list());
    }

    @GetMapping("/{id}")
    public Result<TaskVO> getById(@PathVariable("id") Long id) {
        return Result.success(taskService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<TaskVO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        return Result.success(taskService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<TaskVO> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request
    ) {
        return Result.success(taskService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        taskService.delete(id);
        return Result.success();
    }
}
