package com.zpan.devops.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "devops.jwt")
public class JwtProperties {
    private String secret;

    private Long expireSeconds;
}
