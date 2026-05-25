package com.zpan.devops.pipeline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.pipeline.entity.Runner;
import com.zpan.devops.pipeline.enums.RunnerStatus;
import com.zpan.devops.pipeline.mapper.RunnerMapper;
import com.zpan.devops.pipeline.model.request.RunnerHeartbeatRequest;
import com.zpan.devops.pipeline.model.request.RunnerRegisterRequest;
import com.zpan.devops.pipeline.model.vo.RunnerVO;
import com.zpan.devops.pipeline.service.RunnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RunnerServiceImpl implements RunnerService {

    private final RunnerMapper runnerMapper;

    @Override
    public RunnerVO register(RunnerRegisterRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Runner runner = getByRunnerName(request.getRunnerName());

        if (runner == null) {
            runner = new Runner();
            runner.setRunnerName(request.getRunnerName());
            runner.setRunnerToken(request.getRunnerToken());
            runner.setIp(request.getIp());
            runner.setPort(request.getPort());
            runner.setStatus(RunnerStatus.ONLINE.name());
            runner.setMaxConcurrency(request.getMaxConcurrency());
            runner.setCurrentConcurrency(0);
            runner.setLastHeartbeatAt(now);
            runner.setRegisteredAt(now);
            runner.setCreatedAt(now);
            runner.setUpdatedAt(now);
            runnerMapper.insert(runner);
            return toVO(runner);
        }

        if (!runner.getRunnerToken().equals(request.getRunnerToken())) {
            throw new BizException(ErrorCode.RUNNER_TOKEN_INVALID);
        }

        if (RunnerStatus.DISABLED.name().equals(runner.getStatus())) {
            throw new BizException(ErrorCode.RUNNER_DISABLED);
        }

        runner.setIp(request.getIp());
        runner.setPort(request.getPort());
        runner.setStatus(RunnerStatus.ONLINE.name());
        runner.setMaxConcurrency(request.getMaxConcurrency());
        runner.setLastHeartbeatAt(now);
        runner.setUpdatedAt(now);
        runnerMapper.updateById(runner);

        return toVO(runner);
    }

    @Override
    public RunnerVO heartbeat(RunnerHeartbeatRequest request) {
        Runner runner = getByRunnerName(request.getRunnerName());

        if (runner == null) {
            throw new BizException(ErrorCode.RUNNER_NOT_FOUND);
        }

        if (!runner.getRunnerToken().equals(request.getRunnerToken())) {
            throw new BizException(ErrorCode.RUNNER_TOKEN_INVALID);
        }

        if (RunnerStatus.DISABLED.name().equals(runner.getStatus())) {
            throw new BizException(ErrorCode.RUNNER_DISABLED);
        }

        int currentConcurrency = request.getCurrentConcurrency() == null
                ? runner.getCurrentConcurrency()
                : request.getCurrentConcurrency();

        String status;
        if (currentConcurrency >= runner.getMaxConcurrency()) {
            status = RunnerStatus.BUSY.name();
        } else {
            status = RunnerStatus.ONLINE.name();
        }

        LocalDateTime now = LocalDateTime.now();
        runner.setCurrentConcurrency(currentConcurrency);
        runner.setStatus(status);
        runner.setLastHeartbeatAt(now);
        runner.setUpdatedAt(now);

        runnerMapper.updateById(runner);
        return toVO(runner);
    }

    @Override
    public List<RunnerVO> list() {
        LambdaQueryWrapper<Runner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Runner::getUpdatedAt);

        return runnerMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public RunnerVO getById(Long id) {
        Runner runner = runnerMapper.selectById(id);
        if (runner == null) {
            throw new BizException(ErrorCode.RUNNER_NOT_FOUND);
        }
        return toVO(runner);
    }

    @Override
    public RunnerVO updateStatus(Long id, String status) {
        if (!RunnerStatus.isValid(status)) {
            throw new BizException(ErrorCode.RUNNER_STATUS_INVALID);
        }
        Runner runner = runnerMapper.selectById(id);
        if (runner == null) {
            throw new BizException(ErrorCode.RUNNER_NOT_FOUND);
        }

        runner.setStatus(status);
        runner.setUpdatedAt(LocalDateTime.now());
        runnerMapper.updateById(runner);
        return toVO(runner);
    }

    private Runner getByRunnerName(String runnerName) {
        LambdaQueryWrapper<Runner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Runner::getRunnerName, runnerName);
        return runnerMapper.selectOne(wrapper);

    }

    private RunnerVO toVO(Runner runner) {
        RunnerVO vo = new RunnerVO();

        vo.setId(runner.getId());
        vo.setRunnerName(runner.getRunnerName());
        vo.setIp(runner.getIp());
        vo.setPort(runner.getPort());
        vo.setStatus(runner.getStatus());
        vo.setMaxConcurrency(runner.getMaxConcurrency());
        vo.setCurrentConcurrency(runner.getCurrentConcurrency());
        vo.setLastHeartbeatAt(runner.getLastHeartbeatAt());
        vo.setRegisteredAt(runner.getRegisteredAt());
        vo.setCreatedAt(runner.getCreatedAt());
        vo.setUpdatedAt(runner.getUpdatedAt());

        if (RunnerStatus.isValid(runner.getStatus())) {
            vo.setStatusDescription(RunnerStatus.valueOf(runner.getStatus()).getDescription());
        }

        return vo;

    }
}
