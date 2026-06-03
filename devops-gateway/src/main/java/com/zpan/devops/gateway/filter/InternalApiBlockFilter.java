package com.zpan.devops.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.zpan.devops.gateway.util.GatewayResponseWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalApiBlockFilter implements GlobalFilter, Ordered {

    private final GatewayResponseWriter gatewayResponseWriter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isInternalApiRequest(path)) {
            log.warn("Blocked external internal api request, path={}", path);
            return gatewayResponseWriter.writeJson(exchange, HttpStatus.FORBIDDEN, 403, "禁止访问内部接口");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isInternalApiRequest(String path) {
        if (path == null ||  path.isBlank()) {
            return false;
        }
        return path.contains("/internal/");
    }
}
