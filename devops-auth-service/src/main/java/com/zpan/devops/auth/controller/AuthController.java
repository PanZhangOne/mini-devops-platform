package com.zpan.devops.auth.controller;

import com.zpan.devops.auth.model.request.LoginRequest;
import com.zpan.devops.auth.model.request.RegisterRequest;
import com.zpan.devops.auth.model.vo.LoginVO;
import com.zpan.devops.auth.model.vo.UserVO;
import com.zpan.devops.auth.service.impl.AuthService;
import com.zpan.devops.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
}
