package com.zpan.devops.gateway.security;

import com.zpan.devops.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final GatewayJwtProperties jwtProperties;

    public JwtUserInfo parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        JwtUserInfo userInfo = new JwtUserInfo();
        Object userIdValue = claims.get("userId");
        if (userIdValue instanceof Integer value) {
            userInfo.setUserId(value.longValue());
        } else if (userIdValue instanceof Long value) {
            userInfo.setUserId(value);
        } else if (userIdValue instanceof String value && !value.isBlank()) {
            userInfo.setUserId(Long.valueOf(value));
        }
        Object usernameValue = claims.get("username");
        if (userIdValue != null) {
            userInfo.setUsername(usernameValue.toString());
        } else {
            userInfo.setUsername(claims.getSubject());
        }

        return userInfo;
    }
}
