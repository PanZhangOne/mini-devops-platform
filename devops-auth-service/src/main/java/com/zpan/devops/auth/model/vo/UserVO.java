package com.zpan.devops.auth.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;

    private String username;

    private String nickname;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
