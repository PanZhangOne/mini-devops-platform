package com.zpan.devops.code.controller;

import com.zpan.devops.code.model.request.RepositoryCreateRequest;
import com.zpan.devops.code.model.request.RepositoryListRequest;
import com.zpan.devops.code.model.request.RepositoryUpdateRequest;
import com.zpan.devops.code.model.vo.CodeRepositoryVO;
import com.zpan.devops.code.service.RepositoryService;
import com.zpan.devops.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    public Result<CodeRepositoryVO> create(
            @Valid @RequestBody RepositoryCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        return Result.success(repositoryService.create(request, currentUserId));
    }

    @GetMapping
    public Result<?> list(RepositoryListRequest request) {
        return Result.success(repositoryService.list(request));
    }

    @GetMapping("/{id}")
    public Result<CodeRepositoryVO> getById(@PathVariable("id") Long id) {
        return Result.success(repositoryService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<CodeRepositoryVO> update(
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
