package com.zpan.devops.runner.service;

import com.zpan.devops.common.response.Result;
import com.zpan.devops.runner.client.PipelineRunnerClient;
import com.zpan.devops.runner.config.RunnerProperties;
import com.zpan.devops.runner.model.PipelineRunnerHeartbeatRequest;
import com.zpan.devops.runner.model.PipelineRunnerRegisterRequest;
import com.zpan.devops.runner.model.PipelineRunnerVO;
import com.zpan.devops.runner.runtime.RunnerRuntimeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunnerAgentService implements ApplicationRunner {

    private final RunnerProperties runnerProperties;

    private final PipelineRunnerClient pipelineRunnerClient;

    private final RunnerRuntimeState runnerRuntimeState;

    @Override
    public void run(ApplicationArguments args) {
        register();
    }

    public void register() {
        PipelineRunnerRegisterRequest request = new PipelineRunnerRegisterRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());
        request.setIp(runnerProperties.getIp());
        request.setPort(runnerProperties.getPort());
        request.setMaxConcurrency(runnerProperties.getMaxConcurrency());

        Result<PipelineRunnerVO> result = pipelineRunnerClient.register(request);
        if (result == null || result.getCode() == null || result.getCode() != 0) {
            String message = result == null ? "Runner注册失败" : result.getMessage();
            throw new IllegalStateException(message);
        }
        log.info("Runner registered successfully, runnerName={}, status={}",
                result.getData().getRunnerName(),
                result.getData().getStatus()
        );
    }

    @Scheduled(fixedDelayString = "${runner.heartbeat-interval-ms:10000}")
    public void heartbeat() {
        PipelineRunnerHeartbeatRequest request = new PipelineRunnerHeartbeatRequest();
        request.setRunnerName(runnerProperties.getName());
        request.setRunnerToken(runnerProperties.getToken());
        request.setCurrentConcurrency(runnerRuntimeState.getCurrentConcurrency());

        try {
            Result<PipelineRunnerVO> result = pipelineRunnerClient.heartbeat(request);
            if (result == null || result.getCode() == null || result.getCode() != 0) {
                String message = result == null ? "Runner心跳失败" : result.getMessage();
                log.warn("Runner heartbeat failed, message={}", message);
                return;
            }

            log.debug("Runner heartbeat success, runnerName={}, status={}, currentConcurrency={}",
                    result.getData().getRunnerName(),
                    result.getData().getStatus(),
                    result.getData().getCurrentConcurrency()
            );
        } catch (Exception e) {
            log.warn("Runner heartbeat exception", e);
        }
    }
}
