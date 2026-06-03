package com.zpan.devops.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AccessLogGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String query = exchange.getRequest().getURI().getQuery();
        String requestId = exchange.getRequest().getHeaders().getFirst(RequestIdGlobalFilter.REQUEST_ID_HEADER);
        String remoteAddress = exchange.getRequest().getRemoteAddress() == null ? null : exchange.getRequest().getRemoteAddress().toString();

        return  chain.filter(exchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;
            HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
            log.info(
                    "Gateway access log, requestId={}, method={}, path={}, query={}, status={}, durationMs={}, remoteAddress={}",
                    requestId,
                    method,
                    path,
                    query,
                    statusCode == null ? null : statusCode.value(),
                    duration,
                    remoteAddress
            );
        });
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
