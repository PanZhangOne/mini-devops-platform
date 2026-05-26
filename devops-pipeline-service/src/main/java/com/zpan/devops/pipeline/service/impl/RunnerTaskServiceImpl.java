package com.zpan.devops.pipeline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.pipeline.entity.PipelineLog;
import com.zpan.devops.pipeline.entity.PipelineRun;
import com.zpan.devops.pipeline.entity.PipelineStepRun;
import com.zpan.devops.pipeline.entity.Runner;
import com.zpan.devops.pipeline.enums.PipelineLogLevel;
import com.zpan.devops.pipeline.enums.PipelineRunStatus;
import com.zpan.devops.pipeline.enums.PipelineStepType;
import com.zpan.devops.pipeline.enums.RunnerStatus;
import com.zpan.devops.pipeline.mapper.PipelineLogMapper;
import com.zpan.devops.pipeline.mapper.PipelineRunMapper;
import com.zpan.devops.pipeline.mapper.PipelineStepRunMapper;
import com.zpan.devops.pipeline.mapper.RunnerMapper;
import com.zpan.devops.pipeline.model.request.*;
import com.zpan.devops.pipeline.model.vo.PipelineLogVO;
import com.zpan.devops.pipeline.model.vo.PipelineStepRunVO;
import com.zpan.devops.pipeline.model.vo.RunnerTaskVO;
import com.zpan.devops.pipeline.service.LogStreamService;
import com.zpan.devops.pipeline.service.RunnerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RunnerTaskServiceImpl implements RunnerTaskService {

    private final RunnerMapper runnerMapper;

    private final PipelineRunMapper pipelineRunMapper;

    private final PipelineStepRunMapper pipelineStepRunMapper;

    private final PipelineLogMapper pipelineLogMapper;

    private final LogStreamService logStreamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RunnerTaskVO fetchNext(RunnerTaskNextRequest request) {
        Runner runner = validateRunner(request.getRunnerName(), request.getRunnerToken());

        if (RunnerStatus.BUSY.name().equals(runner.getStatus())) {
            return null;
        }

        List<PipelineRun> candidates = findPendingRunCandidates();

        for (PipelineRun candidate : candidates) {
            LocalDateTime now = LocalDateTime.now();

            int updated = pipelineRunMapper.claimPendingRun(
                    candidate.getId(),
                    PipelineRunStatus.PENDING.name(),
                    PipelineRunStatus.RUNNING.name(),
                    runner.getRunnerName(),
                    now,
                    now
            );

            if (updated == 0) {
                continue;
            }

            PipelineRun claimedRun = getRunOrThrow(candidate.getId());
            appendLog(claimedRun.getId(), null, PipelineLogLevel.INFO.name(), "Runner " + runner.getRunnerName() + " 已经领取流水线任务");
            return toRunnerTaskVO(claimedRun, listStepRuns(candidate.getId()));
        }

        return null;
    }

    @Override
    public void startStep(Long pipelineRunId, Long stepRunId, RunnerTaskStepStartRequest request) {
        validateRunner(request.getRunnerName(), request.getRunnerToken());

        PipelineRun run = getRunOrThrow(pipelineRunId);
        validateRunAssignedToRunner(run, request.getRunnerName());

        PipelineStepRun stepRun = getStepRunOrThrow(stepRunId);
        validateStepBelongsToRun(stepRun, run.getId());

        LocalDateTime now = LocalDateTime.now();
        stepRun.setStatus(PipelineRunStatus.RUNNING.name());
        stepRun.setStartedAt(now);
        stepRun.setUpdatedAt(now);

        pipelineStepRunMapper.updateById(stepRun);
        appendLog(
                run.getId(),
                stepRun.getId(),
                PipelineLogLevel.INFO.name(),
                "开始执行步骤: " + stepRun.getName()
        );
    }

    @Override
    public void finishStep(Long pipelineRunId, Long stepRunId, RunnerTaskStepFinishRequest request) {
        Runner runner = validateRunner(request.getRunnerName(), request.getRunnerToken());

        if (!PipelineRunStatus.isValid(request.getStatus())) {
            throw new BizException(ErrorCode.PIPELINE_RUN_STATUS_INVALID);
        }
        PipelineRun run = getRunOrThrow(pipelineRunId);
        validateRunAssignedToRunner(run, runner.getRunnerName());

        PipelineStepRun stepRun = getStepRunOrThrow(stepRunId);
        validateStepBelongsToRun(stepRun, run.getId());

        LocalDateTime now = LocalDateTime.now();
        stepRun.setStatus(request.getStatus());
        stepRun.setExitCode(request.getExitCode());
        stepRun.setErrorMessage(request.getErrorMessage());
        stepRun.setFinishedAt(now);

        if (stepRun.getStartedAt() != null) {
            stepRun.setDurationSeconds(Duration.between(stepRun.getStartedAt(), now).toSeconds());
        }

        stepRun.setUpdatedAt(now);
        pipelineStepRunMapper.updateById(stepRun);

        appendLog(
                run.getId(),
                stepRun.getId(),
                PipelineLogLevel.INFO.name(),
                "步骤执行结束：" + stepRun.getName() + "，状态：" + request.getStatus()
        );
    }

    @Override
    public void appendLog(Long pipelineRunId, RunnerTaskLogAppendRequest request) {
        validateRunner(request.getRunnerName(), request.getRunnerToken());
        getRunOrThrow(pipelineRunId);

        if (request.getStepRunId() != null) {
            PipelineStepRun stepRun = getStepRunOrThrow(request.getStepRunId());
            validateStepBelongsToRun(stepRun, pipelineRunId);
        }

        appendLog(
                pipelineRunId,
                request.getStepRunId(),
                request.getLogLevel(),
                request.getContent()
        );
    }

    @Override
    public void finishRun(Long pipelineRunId, RunnerTaskFinishRequest request) {
        Runner runner  = validateRunner(request.getRunnerName(), request.getRunnerToken());
        if (!PipelineRunStatus.isValid(request.getStatus())) {
            throw new BizException(ErrorCode.PIPELINE_RUN_STATUS_INVALID);
        }

        PipelineRun run = getRunOrThrow(pipelineRunId);
        validateRunAssignedToRunner(run, runner.getRunnerName());

        LocalDateTime now = LocalDateTime.now();

        run.setStatus(request.getStatus());
        run.setFinishedAt(now);

        if (run.getStartedAt() != null) {
            run.setDurationSeconds(Duration.between(run.getStartedAt(), now).toSeconds());
        }

        run.setUpdatedAt(now);
        pipelineRunMapper.updateById(run);

        String message = "流水线运行结束，状态：" + request.getStatus();
        if (request.getErrorMessage() != null && !request.getErrorMessage().isBlank()) {
            message = message + "，错误信息：" + request.getErrorMessage();
        }

        appendLog(
                run.getId(),
                null,
                PipelineLogLevel.INFO.name(),
                message
        );
        logStreamService.publishComplete(run.getId(), request.getStatus());
    }

    @Override
    public void updateWorkspace(Long pipelineRunId, RunnerTaskWorkspaceUpdateRequest request) {
        Runner runner = validateRunner(request.getRunnerName(), request.getRunnerToken());
        PipelineRun run = getRunOrThrow(pipelineRunId);

        validateRunAssignedToRunner(run, runner.getRunnerName());
        run.setWorkspaceDir(request.getWorkspaceDir());
        run.setUpdatedAt(LocalDateTime.now());

        pipelineRunMapper.updateById(run);
        appendLog(run.getId(), null, PipelineLogLevel.INFO.name(), "工作目录已经创建: " + request.getWorkspaceDir());
    }

    private void validateRunAssignedToRunner(PipelineRun run, String runnerName) {
        if (run.getAssignedRunnerName() == null || !run.getAssignedRunnerName().equals(runnerName)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "当前Runner无权操作该流水线运行");
        }
    }

    private Runner validateRunner(String runnerName, String runnerToken) {

        LambdaQueryWrapper<Runner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Runner::getRunnerName, runnerName);
        Runner runner = runnerMapper.selectOne(wrapper);

        if (runner == null) {
            throw new BizException(ErrorCode.RUNNER_NOT_FOUND);
        }
        if (!runner.getRunnerToken().equals(runnerToken)) {
            throw new BizException(ErrorCode.RUNNER_TOKEN_INVALID);
        }

        if (RunnerStatus.DISABLED.name().equals(runner.getStatus())) {
            throw new BizException(ErrorCode.RUNNER_DISABLED);
        }

        return runner;
    }

    private PipelineRun findOldestPendingRun() {
        LambdaQueryWrapper<PipelineRun> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineRun::getStatus, PipelineRunStatus.PENDING.name());
        wrapper.orderByAsc(PipelineRun::getCreatedAt);
        wrapper.last("LIMIT 1");

        return pipelineRunMapper.selectOne(wrapper);
    }

    private List<PipelineRun> findPendingRunCandidates() {
        LambdaQueryWrapper<PipelineRun> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineRun::getStatus, PipelineRunStatus.PENDING.name());
        wrapper.orderByAsc(PipelineRun::getCreatedAt);
        wrapper.last("LIMIT 10");

        return pipelineRunMapper.selectList(wrapper);
    }

    private PipelineRun getRunOrThrow(Long pipelineRunId) {
        PipelineRun run = pipelineRunMapper.selectById(pipelineRunId);
        if (run == null) {
            throw new BizException(ErrorCode.PIPELINE_RUN_NOT_FOUND);
        }
        return run;
    }

    private PipelineStepRun getStepRunOrThrow(Long stepRunId) {
        PipelineStepRun stepRun = pipelineStepRunMapper.selectById(stepRunId);
        if (stepRun == null) {
            throw new BizException(ErrorCode.PIPELINE_STEP_NOT_FOUND);
        }

        return stepRun;
    }

    private void validateStepBelongsToRun(PipelineStepRun stepRun, Long pipelineRunId) {
        if (!pipelineRunId.equals(stepRun.getPipelineRunId())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "步骤运行记录不属于当前流水线运行");
        }
    }

    private List<PipelineStepRun> listStepRuns(Long pipelineRunId) {
        LambdaQueryWrapper<PipelineStepRun> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineStepRun::getPipelineRunId, pipelineRunId);
        wrapper.orderByAsc(PipelineStepRun::getSortOrder);

        return pipelineStepRunMapper.selectList(wrapper);
    }

    private void appendLog(Long pipelineRunId, Long stepRunId, String logLevel, String content) {

        LocalDateTime now = LocalDateTime.now();

        PipelineLog log = new PipelineLog();
        log.setPipelineRunId(pipelineRunId);
        log.setStepRunId(stepRunId);
        log.setLogTime(now);
        log.setLogLevel(logLevel);
        log.setContent(content);
        log.setCreatedAt(now);

        pipelineLogMapper.insert(log);

        PipelineLogVO logVO = new PipelineLogVO();
        logVO.setId(log.getId());
        logVO.setPipelineRunId(pipelineRunId);
        logVO.setStepRunId(stepRunId);
        logVO.setLogTime(now);
        logVO.setLogLevel(logLevel);
        logVO.setContent(content);
        logVO.setCreatedAt(now);

        logStreamService.publishLog(pipelineRunId, logVO);
    }

    private RunnerTaskVO toRunnerTaskVO(PipelineRun run, List<PipelineStepRun> stepRuns) {
        RunnerTaskVO vo = new RunnerTaskVO();

        vo.setPipelineRunId(run.getId());
        vo.setRunNo(run.getRunNo());
        vo.setPipelineId(run.getPipelineId());
        vo.setProjectId(run.getProjectId());
        vo.setRepositoryId(run.getRepositoryId());
        vo.setVersionId(run.getVersionId());
        vo.setBranchName(run.getBranchName());
        vo.setCommitHash(run.getCommitHash());
        vo.setImageTag(run.getImageTag());
        vo.setEnv(run.getEnv());
        vo.setSteps(stepRuns.stream().map(this::toStepRunVO).toList());

        return vo;

    }

    private PipelineStepRunVO toStepRunVO(PipelineStepRun stepRun) {
        PipelineStepRunVO vo = new PipelineStepRunVO();

        vo.setId(stepRun.getId());
        vo.setPipelineRunId(stepRun.getPipelineRunId());
        vo.setPipelineStepId(stepRun.getPipelineStepId());
        vo.setName(stepRun.getName());
        vo.setStepType(stepRun.getStepType());
        vo.setSortOrder(stepRun.getSortOrder());
        vo.setCommand(stepRun.getCommand());
        vo.setConfigJson(stepRun.getConfigJson());
        vo.setStatus(stepRun.getStatus());
        vo.setStartedAt(stepRun.getStartedAt());
        vo.setFinishedAt(stepRun.getFinishedAt());
        vo.setDurationSeconds(stepRun.getDurationSeconds());
        vo.setExitCode(stepRun.getExitCode());
        vo.setErrorMessage(stepRun.getErrorMessage());
        vo.setCreatedAt(stepRun.getCreatedAt());
        vo.setUpdatedAt(stepRun.getUpdatedAt());

        if (PipelineStepType.isValid(stepRun.getStepType())) {
            vo.setStepTypeDescription(PipelineStepType.valueOf(stepRun.getStepType()).getDescription());
        }

        if (PipelineRunStatus.isValid(stepRun.getStatus())) {
            vo.setStatusDescription(PipelineRunStatus.valueOf(stepRun.getStatus()).getDescription());
        }

        return vo;
    }
}
