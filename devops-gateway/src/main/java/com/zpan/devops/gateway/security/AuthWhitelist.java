package com.zpan.devops.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component
public class AuthWhitelist {
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> whitelist = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/captcha",
            "/actuator/health",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    public List<String> getWhitelist() {
        return whitelist;
    }


    public boolean isWhitelisted(ServerWebExchange exchange) {
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return true;
        }

        String path = exchange.getRequest().getURI().getPath();
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
