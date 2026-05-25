package com.zpan.devops.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.auth.config.JwtProperties;
import com.zpan.devops.auth.entity.SysUser;
import com.zpan.devops.auth.enums.UserStatus;
import com.zpan.devops.auth.mapper.SysUserMapper;
import com.zpan.devops.auth.model.request.LoginRequest;
import com.zpan.devops.auth.model.request.RegisterRequest;
import com.zpan.devops.auth.model.vo.LoginVO;
import com.zpan.devops.auth.model.vo.UserVO;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtProperties jwtProperties;

    @Override
    public UserVO register(RegisterRequest request) {
        validateUsernameNotExists(request.getUsername());

        LocalDateTime now = LocalDateTime.now();

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(UserStatus.ENABLED.name());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        sysUserMapper.insert(user);

        return toUserVO(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = getByUsername(request.getUsername());

        if (user == null) {
            throw new BizException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (UserStatus.DISABLED.name().equals(user.getStatus())) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }

        String token = JwtUtils.generateToken(
                user.getId(),
                user.getUsername(),
                jwtProperties.getSecret(),
                jwtProperties.getExpireSeconds()
        );

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());

        return loginVO;
    }

    private SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }

    private void validateUsernameNotExists(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        Long count = sysUserMapper.selectCount(wrapper);

        if (count > 0) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
    }

    private UserVO toUserVO(SysUser user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setStatus(user.getStatus());
        userVO.setCreatedAt(user.getCreatedAt());
        userVO.setUpdatedAt(user.getUpdatedAt());

        return userVO;
    }
}
