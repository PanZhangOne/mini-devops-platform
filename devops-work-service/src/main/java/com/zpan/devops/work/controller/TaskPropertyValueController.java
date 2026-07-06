package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.request.TaskPropertyValueSaveRequest;
import com.zpan.devops.work.model.vo.TaskPropertyValueVO;
import com.zpan.devops.work.service.TaskPropertyValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskPropertyValueController {

    private final TaskPropertyValueService taskPropertyValueService;

    @PutMapping("/tasks/{taskId}/property-values")
    public Result<Void> saveValues(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskPropertyValueSaveRequest request
    ) {
        taskPropertyValueService.saveValues(taskId, request);
        return Result.success();
    }

    @GetMapping("/tasks/{taskId}/property-values")
    public Result<List<TaskPropertyValueVO>> listByTaskId(@PathVariable Long taskId) {
        return Result.success(taskPropertyValueService.listByTaskId(taskId));
    }
}
