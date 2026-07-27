package com.zpan.devops.code.controller;

import com.zpan.devops.code.model.vo.CodeBranchVO;
import com.zpan.devops.code.service.RepositoryBranchService;
import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/repositories/{repositoryId}")
public class RepositoryBranchController {

    private final RepositoryBranchService repositoryBranchService;

    @GetMapping("/branches")
    public Result<List<CodeBranchVO>> list(@PathVariable Long repositoryId) {
        return Result.success(repositoryBranchService.listByRepositoryId(repositoryId));
    }

    @GetMapping("/branch")
    public Result<CodeBranchVO> getByBranchName(@PathVariable Long repositoryId, @RequestParam String branchName) {
        return Result.success(repositoryBranchService.getByBranchName(repositoryId, branchName));
    }
}
