package com.zpan.devops.code.controller;

import com.zpan.devops.code.client.WorkProjectClient;
import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign-test")
@RequiredArgsConstructor
public class FeignTestController {

    private final WorkProjectClient workProjectClient;

    @GetMapping("/projects/{id}/exists")
    public Result<Boolean> exists(@PathVariable("id") Long id) {
        return workProjectClient.existsById(id);
    }

    @GetMapping("/projects/{id}/exists-slow")
    public Result<Boolean> existsSlow(@PathVariable("id") Long id) {
        return workProjectClient.existsByIdSlow(id);
    }
}
