package com.zpan.devops.code.controller.internal;

import com.zpan.devops.code.service.RepositoryService;
import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/repositories")
@RequiredArgsConstructor
public class InternalRepositoryController {

    private final RepositoryService repositoryService;

    @GetMapping("/{id}/exists")
    public Result<Boolean> existsById(@PathVariable("id") Long id) {
        return Result.success(repositoryService.existsById(id));
    }
}
