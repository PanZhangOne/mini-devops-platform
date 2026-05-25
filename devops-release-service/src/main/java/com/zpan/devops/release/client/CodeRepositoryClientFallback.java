package com.zpan.devops.release.client;

import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CodeRepositoryClientFallback implements CodeRepositoryClient {
    @Override
    public Result<Boolean> existsById(Long id) {
        log.warn("Fallback triggered: devops-code-service existsById failed, repositoryId={}", id);
        return Result.fail(
                ErrorCode.REMOTE_SERVICE_ERROR.getCode(),
                "代码仓库服务暂不可用，请稍后再试"
        );
    }
}
