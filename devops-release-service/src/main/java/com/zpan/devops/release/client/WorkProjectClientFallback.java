package com.zpan.devops.release.client;

import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkProjectClientFallback implements WorkProjectClient {
    @Override
    public Result<Boolean> existsById(Long id) {
        log.warn("Fallback triggered: devops-work-service existsById failed, projectId={}", id);
        return Result.fail(
                ErrorCode.REMOTE_SERVICE_ERROR.getCode(),
                "项目服务暂时不可用，请稍候重试"
        );
    }
}
