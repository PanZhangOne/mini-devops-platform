package com.zpan.devops.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "devops.jwt")
public class GatewayJwtProperties {

    private String secret;
}
