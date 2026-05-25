package com.zpan.devops.auth.service.impl;

import com.zpan.devops.auth.model.request.LoginRequest;
import com.zpan.devops.auth.model.request.RegisterRequest;
import com.zpan.devops.auth.model.vo.LoginVO;
import com.zpan.devops.auth.model.vo.UserVO;

public interface AuthService {

    UserVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);
}
