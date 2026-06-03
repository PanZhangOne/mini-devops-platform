package com.zpan.devops.gateway.security;

import lombok.Data;

@Data
public class JwtUserInfo {

    private Long userId;

    private String username;
}
