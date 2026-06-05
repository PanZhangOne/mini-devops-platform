package com.zpan.devops.work.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.model.vo.TaskActivityVO;
import com.zpan.devops.work.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @GetMapping("/{id}/activities")
    public Result<List<TaskActivityVO>> getTaskActivities(@PathVariable("taskId") Long taskId) {
        List<TaskActivityVO> list = taskActivityService.listByTaskId(taskId);
        return Result.success(list);
    }
}
