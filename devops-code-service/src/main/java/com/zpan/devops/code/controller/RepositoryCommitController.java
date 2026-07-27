package com.zpan.devops.code.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpan.devops.code.model.request.CommitListRequest;
import com.zpan.devops.code.model.vo.CodeCommitVO;
import com.zpan.devops.code.service.RepositoryCommitService;
import com.zpan.devops.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/repositories/{repositoryId}")
public class RepositoryCommitController {

    private final RepositoryCommitService repositoryCommitService;

    @GetMapping("/commits")
    public Result<Page<CodeCommitVO>> list(@PathVariable Long repositoryId, @Valid CommitListRequest request) {
        return Result.success(repositoryCommitService.list(repositoryId, request));
    }

    @GetMapping("/commits/{commitHash}")
    public Result<CodeCommitVO> getByCommitHash(@PathVariable Long repositoryId, @PathVariable String commitHash) {
        return Result.success(repositoryCommitService.getByCommitHash(repositoryId, commitHash));
    }
}
