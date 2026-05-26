package com.zpan.devops.code.controller;

import com.zpan.devops.code.model.request.RepositoryCreateRequest;
import com.zpan.devops.code.model.request.RepositoryUpdateRequest;
import com.zpan.devops.code.model.vo.RepositoryVO;
import com.zpan.devops.code.service.RepositoryService;
import com.zpan.devops.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    public Result<RepositoryVO> create(@Valid @RequestBody RepositoryCreateRequest request) {
        return Result.success(repositoryService.create(request));
    }

    @GetMapping
    public Result<List<RepositoryVO>> list(@RequestParam(value = "projectId", required = false) Long projectId) {
        if (projectId != null) {
            return Result.success(repositoryService.listByProjectId(projectId));
        }
        return Result.success(repositoryService.list());
    }

    @GetMapping("/{id}")
    public Result<RepositoryVO> getById(@PathVariable("id") Long id) {
        return Result.success(repositoryService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<RepositoryVO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody RepositoryUpdateRequest request
    ) {
        return Result.success(repositoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        repositoryService.delete(id);
        return Result.success();
    }
}
