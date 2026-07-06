package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.vo.TaskDetailVO;
import com.zpan.devops.work.service.TaskDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TaskDetailController {

    private final TaskDetailService taskDetailService;

    @GetMapping("/tasks/{id}/detail")
    public Result<TaskDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(taskDetailService.getDetail(id));
    }
}
