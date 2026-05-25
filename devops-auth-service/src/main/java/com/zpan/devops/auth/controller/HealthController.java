package com.zpan.devops.auth.controller;

import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
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
}
