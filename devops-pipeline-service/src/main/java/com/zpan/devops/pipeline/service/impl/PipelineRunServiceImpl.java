package com.zpan.devops.pipeline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.pipeline.entity.*;
import com.zpan.devops.pipeline.enums.PipelineLogLevel;
import com.zpan.devops.pipeline.enums.PipelineRunStatus;
import com.zpan.devops.pipeline.enums.PipelineStepType;
import com.zpan.devops.pipeline.enums.PipelineTriggerType;
import com.zpan.devops.pipeline.mapper.*;
import com.zpan.devops.pipeline.model.request.PipelineRunCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineRunQueryRequest;
import com.zpan.devops.pipeline.model.vo.*;
import com.zpan.devops.pipeline.service.LogStreamService;
import com.zpan.devops.pipeline.service.PipelineRunService;
import com.zpan.devops.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PipelineRunServiceImpl implements PipelineRunService {

    private final PipelineService pipelineService;

    private final PipelineStepMapper pipelineStepMapper;

    private final PipelineRunMapper pipelineRunMapper;

    private final PipelineStepRunMapper pipelineStepRunMapper;

    private final PipelineLogMapper pipelineLogMapper;

    private final LogStreamService logStreamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineRunVO create(Long pipelineId, PipelineRunCreateRequest request, Long currentUserId) {
        Pipeline pipeline = pipelineService.getEntityById(pipelineId);

        if (Boolean.FALSE.equals(pipeline.getEnabled())) {
            throw new BizException(ErrorCode.PIPELINE_DISABLED);
        }

        List<PipelineStep> enabledSteps = listEnabledSteps(pipelineId);
        if (enabledSteps.isEmpty()) {
            throw new BizException(ErrorCode.PIPELINE_DISABLED);
        }

        LocalDateTime now = LocalDateTime.now();


        PipelineRun run = new PipelineRun();

        run.setPipelineId(pipeline.getId());
        run.setProjectId(pipeline.getProjectId());
        run.setRepositoryId(pipeline.getRepositoryId());
        run.setVersionId(request.getVersionId());
        run.setRunNo(generateRunNo());
        run.setBranchName(request.getBranchName());
        run.setCommitHash(request.getCommitHash());
        run.setImageTag(request.getImageTag());
        run.setEnv(request.getEnv());
        run.setStatus(PipelineRunStatus.PENDING.name());
        run.setTriggerUserId(currentUserId);
        run.setTriggerType(PipelineTriggerType.MANUAL.name());
        run.setAssignedRunnerName(null);
        run.setWorkspaceDir(null);
        run.setCreatedAt(now);
        run.setUpdatedAt(now);

        pipelineRunMapper.insert(run);

        for (PipelineStep step : enabledSteps) {
            PipelineStepRun stepRun = new PipelineStepRun();
            stepRun.setPipelineRunId(run.getId());
            stepRun.setPipelineStepId(step.getId());
            stepRun.setName(step.getName());
            stepRun.setSortOrder(step.getSortOrder());
            stepRun.setCommand(step.getCommand());
            stepRun.setConfigJson(step.getConfigJson());
            stepRun.setStatus(PipelineRunStatus.PENDING.name());
            stepRun.setStepType(step.getStepType());
            stepRun.setCreatedAt(now);
            stepRun.setUpdatedAt(now);

            pipelineStepRunMapper.insert(stepRun);

        }

        appendLog(run.getId(), null, PipelineLogLevel.INFO.name(), "流水线运行已创建，等待 Runner 执行");
        return toRunVO(run);
    }

    @Override
    public List<PipelineRunVO> list(PipelineRunQueryRequest query) {
        LambdaQueryWrapper<PipelineRun> wrapper = new LambdaQueryWrapper<>();

        if (query.getPipelineId() != null) {
            wrapper.eq(PipelineRun::getPipelineId, query.getPipelineId());
        }

        if (query.getProjectId() != null) {
            wrapper.eq(PipelineRun::getProjectId, query.getProjectId());
        }

        if (query.getRepositoryId() != null) {
            wrapper.eq(PipelineRun::getRepositoryId, query.getRepositoryId());
        }

        if (query.getVersionId() != null) {
            wrapper.eq(PipelineRun::getVersionId, query.getVersionId());
        }

        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(PipelineRun::getStatus, query.getStatus());
        }

        if (query.getTriggerType() != null && !query.getTriggerType().isBlank()) {
            wrapper.eq(PipelineRun::getTriggerType, query.getTriggerType());
        }

        wrapper.orderByDesc(PipelineRun::getCreatedAt);

        return pipelineRunMapper.selectList(wrapper)
                .stream()
                .map(this::toRunVO)
                .toList();

    }

    @Override
    public PipelineRunVO getById(Long id) {
        PipelineRun run = getRunOrThrow(id);
        return toRunVO(run);
    }

    @Override
    public List<PipelineStepRunVO> listSteps(Long pipelineRunId) {
        getRunOrThrow(pipelineRunId);

        LambdaQueryWrapper<PipelineStepRun> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineStepRun::getPipelineRunId, pipelineRunId);
        wrapper.orderByAsc(PipelineStepRun::getSortOrder);

        return pipelineStepRunMapper.selectList(wrapper)
                .stream()
                .map(this::toStepRunVO)
                .toList();
    }

    @Override
    public List<PipelineLogVO> listLogs(Long pipelineRunId) {
        getRunOrThrow(pipelineRunId);

        LambdaQueryWrapper<PipelineLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineLog::getPipelineRunId, pipelineRunId);
        wrapper.orderByAsc(PipelineLog::getLogTime);
        wrapper.orderByAsc(PipelineLog::getId);

        return pipelineLogMapper.selectList(wrapper)
                .stream()
                .map(this::toLogVO)
                .toList();
    }

    private List<PipelineStep> listEnabledSteps(Long pipelineId) {

        LambdaQueryWrapper<PipelineStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineStep::getPipelineId, pipelineId);
        wrapper.eq(PipelineStep::getEnabled, true);
        wrapper.orderByAsc(PipelineStep::getSortOrder);

        return pipelineStepMapper.selectList(wrapper);
    }

    private PipelineRun getRunOrThrow(Long id) {
        PipelineRun run = pipelineRunMapper.selectById(id);

        if (run == null) {
            throw new BizException(ErrorCode.PIPELINE_RUN_NOT_FOUND);
        }
        return run;
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
        logStreamService.publishLog(pipelineRunId, toLogVO(log));
    }

    private String generateRunNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "PIPE-" + date + "-" + random;
    }



    private PipelineRunVO toRunVO(PipelineRun run) {
        PipelineRunVO vo = new PipelineRunVO();

        vo.setId(run.getId());
        vo.setPipelineId(run.getPipelineId());
        vo.setProjectId(run.getProjectId());
        vo.setRepositoryId(run.getRepositoryId());
        vo.setVersionId(run.getVersionId());
        vo.setRunNo(run.getRunNo());
        vo.setBranchName(run.getBranchName());
        vo.setCommitHash(run.getCommitHash());
        vo.setImageTag(run.getImageTag());
        vo.setEnv(run.getEnv());
        vo.setStatus(run.getStatus());
        vo.setTriggerUserId(run.getTriggerUserId());
        vo.setTriggerType(run.getTriggerType());
        vo.setStartedAt(run.getStartedAt());
        vo.setFinishedAt(run.getFinishedAt());
        vo.setDurationSeconds(run.getDurationSeconds());
        vo.setAssignedRunnerName(run.getAssignedRunnerName());
        vo.setWorkspaceDir(run.getWorkspaceDir());
        vo.setCreatedAt(run.getCreatedAt());
        vo.setUpdatedAt(run.getUpdatedAt());

        if (PipelineRunStatus.isValid(run.getStatus())) {
            vo.setStatusDescription(PipelineRunStatus.valueOf(run.getStatus()).getDescription());
        }
        if (PipelineTriggerType.isValid(run.getTriggerType())) {
            vo.setTriggerTypeDescription(PipelineTriggerType.valueOf(run.getTriggerType()).getDescription());
        }

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

    private PipelineLogVO toLogVO(PipelineLog log) {

        PipelineLogVO vo = new PipelineLogVO();
        vo.setId(log.getId());
        vo.setPipelineRunId(log.getPipelineRunId());
        vo.setStepRunId(log.getStepRunId());
        vo.setLogTime(log.getLogTime());
        vo.setLogLevel(log.getLogLevel());
        vo.setContent(log.getContent());
        vo.setCreatedAt(log.getCreatedAt());

        return vo;

    }
}
