package com.zpan.devops.code.client;

import com.zpan.devops.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "devops-work-service", fallback = WorkProjectClientFallback.class)
public interface WorkProjectClient {

    @GetMapping("/internal/projects/{id}/exists")
    Result<Boolean> existsById(@PathVariable("id") Long id);

    @GetMapping("/internal/projects/{id}/exists-slow")
    Result<Boolean> existsByIdSlow(@PathVariable("id") Long id);
}
