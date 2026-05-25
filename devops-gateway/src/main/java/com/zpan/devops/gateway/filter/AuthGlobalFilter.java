package com.zpan.devops.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import com.zpan.devops.common.security.JwtUtils;
import com.zpan.devops.common.security.LoginUser;
import com.zpan.devops.gateway.config.GatewayAuthProperties;
import com.zpan.devops.gateway.config.GatewayJwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayJwtProperties jwtProperties;

    private final GatewayAuthProperties gatewayAuthProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().toString())) {
            return chain.filter(exchange);
        }

        if (isInternalPath(path)) {
            return writeJson(
                    exchange,
                    HttpStatus.FORBIDDEN,
                    Result.fail(ErrorCode.FORBIDDEN.getCode(), "禁止从网关访问内部接口")
            );
        }

        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank()) {
            return unauthorized(exchange);
        }

        String token = parseBearerToken(authorization);
        if (token == null) {
            return unauthorized(exchange);
        }

        try {
            LoginUser loginUser = JwtUtils.parseToken(token, jwtProperties.getSecret());

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", String.valueOf(loginUser.getUserId()))
                    .header("X-Username", loginUser.getUsername())
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            log.warn("Token parse failed, path={}", path, e);
            return unauthorized(exchange);
        }
    }

    private boolean isInternalPath(String path) {
        return path.contains("/internal/");
    }

    private boolean isWhiteList(String path) {
        if (gatewayAuthProperties.getWhiteList() == null || gatewayAuthProperties.getWhiteList().isEmpty()) {
            return getDefaultWhiteList().stream()
                    .anyMatch(pattern -> matchPath(pattern, path));
        }
        return gatewayAuthProperties.getWhiteList().stream()
                .anyMatch(pattern -> matchPath(pattern, path));
    }

    private boolean matchPath(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    private List<String> getDefaultWhiteList() {
        return List.of("/api/auth/register", "/api/auth/login", "/login", "/register", "/api/auth/ping");
    }

    private String parseBearerToken(String authorization) {
        String prefix = "Bearer ";
        return authorization.substring(prefix.length());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus httpStatus, Result result) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json;
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            json = "{\"code\":50000,\"message\":\"系统内部异常\",\"data\":null}";
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
