package com.zpan.devops.code.controller;

import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/instance")
    public Result<Map<String, Object>> instance() throws Exception {
        String port = environment.getProperty("server.port");
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        Map<String, Object> data = new HashMap<>();
        data.put("applicationName", applicationName);
        data.put("host", hostAddress);
        data.put("port", port);

        return Result.success(data);
    }
}
