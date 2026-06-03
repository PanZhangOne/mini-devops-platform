package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.model.vo.CredentialSecretVO;
import com.zpan.devops.pipeline.service.CredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/credentials")
@RequiredArgsConstructor
public class CredentialInternalController {
    private final CredentialService credentialService;

    @GetMapping("/{id}/secret")
    public Result<CredentialSecretVO> getSecretById(@PathVariable("id") Long id) {
        return Result.success(credentialService.getSecretById(id));
    }
}
