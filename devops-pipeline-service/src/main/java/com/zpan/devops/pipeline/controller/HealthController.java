package com.zpan.devops.pipeline.controller;

import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {
    private final Environment environment;

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/ping")
    public Result<String> ping() {
        String port = environment.getProperty("server.port");
        return Result.success(applicationName + " ok, port=" + port);
    }

    @GetMapping("/ping/user")
    public Result<String> pingUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String userName,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId
    ) {
        return Result.success("ping user success, userId=" + userId + ", userName=" + userName + ", requestId=" + requestId);
    }
}
