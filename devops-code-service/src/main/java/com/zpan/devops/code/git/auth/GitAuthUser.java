package com.zpan.devops.code.git.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitAuthUser {

    private Long userId;

    private String username;
}
