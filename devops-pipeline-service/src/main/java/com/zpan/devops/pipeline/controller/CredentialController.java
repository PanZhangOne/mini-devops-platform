package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.model.request.CredentialCreateRequest;
import com.zpan.devops.pipeline.model.request.CredentialUpdateRequest;
import com.zpan.devops.pipeline.model.vo.CredentialVO;
import com.zpan.devops.pipeline.service.CredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/credentials")
public class CredentialController {
    private final CredentialService credentialService;

    @PostMapping
    public Result<CredentialVO> create(
            @Valid @RequestBody CredentialCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId
    ) {
        return Result.success(credentialService.create(request, currentUserId));
    }

    @GetMapping
    public Result<List<CredentialVO>> list(@RequestParam(required = false) Long projectId) {
        return Result.success(credentialService.list(projectId));
    }

    @GetMapping("/{id}")
    public Result<CredentialVO> getById(@PathVariable Long id) {
        return Result.success(credentialService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<CredentialVO> update(
            @PathVariable Long id,
            @Valid @RequestBody CredentialUpdateRequest request
    ) {
        return Result.success(credentialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        credentialService.delete(id);
        return Result.success();
    }
}
