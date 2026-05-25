package com.zpan.devops.runner.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepConfigParser {

    private final ObjectMapper objectMapper;

    public <T> T parse(String configJson, Class<T> clazz) {
        try {
            if (configJson == null || configJson.isBlank()) {
                return clazz.getDeclaredConstructor().newInstance();
            }
            return objectMapper.readValue(configJson, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("配置JSON解析失败： " + e.getMessage());
        }
    }
}
