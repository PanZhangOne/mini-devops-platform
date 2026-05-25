package com.zpan.devops.release.client;

import com.zpan.devops.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "devops-code-service", fallback = CodeRepositoryClientFallback.class)
public interface CodeRepositoryClient {


    @GetMapping("/internal/repositories/{id}/exists")
    Result<Boolean> existsById(@PathVariable("id") Long id);
}
