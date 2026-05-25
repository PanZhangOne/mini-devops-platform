package com.zpan.devops.work.controller.internal;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.work.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class InternalProjectController {

    private final ProjectService projectService;

    @GetMapping("/{id}/exists")
    public Result<Boolean> existsById(@PathVariable("id") Long id) {
        return Result.success(projectService.existsById(id));
    }

    @GetMapping("/{id}/exists-slow")
    public Result<Boolean> existsByIdSlow(@PathVariable("id") Long id) throws InterruptedException {
        Thread.sleep(5000);
        return Result.success(projectService.existsById(id));
    }
}
