package com.zpan.devops.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties("devops.gateway")
public class GatewayAuthProperties {

    private List<String> whiteList = new ArrayList<>();
}
