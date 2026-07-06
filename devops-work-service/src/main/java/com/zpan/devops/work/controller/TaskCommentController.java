package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.request.TaskCommentCreateRequest;
import com.zpan.devops.work.model.vo.TaskCommentTreeVO;
import com.zpan.devops.work.model.vo.TaskCommentVO;
import com.zpan.devops.work.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;


    @PostMapping("/tasks/{taskId}/comments")
    public Result<TaskCommentVO> create(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(taskCommentService.create(taskId, request, currentUserId));
    }

    @GetMapping("/tasks/{taskId}/comments")
    public Result<List<TaskCommentTreeVO>> treeByTaskId(@PathVariable Long taskId) {
        return Result.success(taskCommentService.treeByTaskId(taskId));
    }

    @DeleteMapping("/task-comments/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        taskCommentService.delete(id, currentUserId);
        return Result.success();
    }
}
