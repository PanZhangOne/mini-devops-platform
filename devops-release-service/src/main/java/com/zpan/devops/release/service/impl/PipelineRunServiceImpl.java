package com.zpan.devops.release.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import com.zpan.devops.release.client.CodeRepositoryClient;
import com.zpan.devops.release.client.WorkProjectClient;
import com.zpan.devops.release.entity.PipelineRun;
import com.zpan.devops.release.enums.PipelineEnv;
import com.zpan.devops.release.enums.PipelineRunStatus;
import com.zpan.devops.release.enums.PipelineTriggerType;
import com.zpan.devops.release.mapper.PipelineRunMapper;
import com.zpan.devops.release.model.request.PipelineRunCreateRequest;
import com.zpan.devops.release.model.request.PipelineRunQueryRequest;
import com.zpan.devops.release.model.request.PipelineRunStatusUpdateRequest;
import com.zpan.devops.release.model.vo.PipelineRunVO;
import com.zpan.devops.release.service.PipelineRunService;
import com.zpan.devops.release.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PipelineRunServiceImpl implements PipelineRunService {


    private final PipelineRunMapper pipelineRunMapper;

    private final VersionService versionService;

    private final WorkProjectClient workProjectClient;

    private final CodeRepositoryClient codeRepositoryClient;

    @Override
    public PipelineRunVO create(PipelineRunCreateRequest request, Long currentUserId) {
        checkProjectExists(request.getProjectId());
        checkRepositoryExists(request.getRepositoryId());
        versionService.checkVersionExists(request.getVersionId());

        validateEnv(request.getEnv());
        validateTriggerType(request.getTriggerType());

        LocalDateTime now = LocalDateTime.now();

        PipelineRun run = new PipelineRun();
        run.setProjectId(request.getProjectId());
        run.setRepositoryId(request.getRepositoryId());
        run.setVersionId(request.getVersionId());
        run.setRunNo(generateRunNo());
        run.setEnv(request.getEnv());
        run.setStatus(PipelineRunStatus.PENDING.name());
        run.setImageTag(request.getImageTag());
        run.setCommitHash(request.getCommitHash());
        run.setTriggerUserId(currentUserId);
        run.setTriggerType(request.getTriggerType());
        run.setLogText("流水线已创建，等待执行");
        run.setCreatedAt(now);
        run.setUpdatedAt(now);

        pipelineRunMapper.insert(run);

        return toVO(run);
    }

    @Override
    public List<PipelineRunVO> list(PipelineRunQueryRequest query) {
        LambdaQueryWrapper<PipelineRun> wrapper = new LambdaQueryWrapper<>();

        if (query.getProjectId() != null) {
            wrapper.eq(PipelineRun::getProjectId, query.getProjectId());
        }

        if (query.getRepositoryId() != null) {
            wrapper.eq(PipelineRun::getRepositoryId, query.getRepositoryId());
        }

        if (query.getVersionId() != null) {
            wrapper.eq(PipelineRun::getVersionId, query.getVersionId());
        }

        if (query.getEnv() != null && !query.getEnv().isBlank()) {
            wrapper.eq(PipelineRun::getEnv, query.getEnv());
        }

        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(PipelineRun::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(PipelineRun::getCreatedAt);

        return pipelineRunMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public PipelineRunVO getById(Long id) {
        PipelineRun run = getPipelineRunOrThrow(id);
        return toVO(run);
    }

    @Override
    public PipelineRunVO updateStatus(Long id, PipelineRunStatusUpdateRequest request) {
        PipelineRun run = getPipelineRunOrThrow(id);

        if (!PipelineRunStatus.isValid(request.getStatus())) {
            throw new BizException(ErrorCode.PIPELINE_RUN_STATUS_INVALID);
        }

        LocalDateTime now = LocalDateTime.now();
        String newStatus = request.getStatus();

        run.setStatus(newStatus);
        run.setUpdatedAt(now);

        if (request.getImageTag() != null && !request.getImageTag().isBlank()) {
            run.setImageTag(request.getImageTag());
        }

        if (request.getLogText() != null) {
            run.setLogText(request.getLogText());
        }

        if (request.getErrorMessage() != null) {
            run.setErrorMessage(request.getErrorMessage());
        }

        if (PipelineRunStatus.RUNNING.name().equals(newStatus) && run.getStartedAt() == null) {
            run.setStartedAt(now);
        }

        if (PipelineRunStatus.isFinished(newStatus)) {
            run.setFinishedAt(now);

            if (run.getStartedAt() != null) {
                run.setDurationSeconds(Duration.between(run.getStartedAt(), now).toSeconds());
            }
        }

        pipelineRunMapper.updateById(run);

        return toVO(run);
    }

    @Override
    public void delete(Long id) {
        PipelineRun run = getPipelineRunOrThrow(id);

        if (PipelineRunStatus.RUNNING.name().equals(run.getStatus())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "运行中的流水线不能删除");
        }

        pipelineRunMapper.deleteById(run.getId());
    }

    private void checkProjectExists(Long projectId) {
        Result<Boolean> result;

        try {
            result = workProjectClient.existsById(projectId);
        } catch (Exception e) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "调用项目服务失败");
        }

        if (result == null || result.getCode() == null) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "项目服务返回为空");
        }

        if (result.getCode() != 0) {
            throw new BizException(result.getCode(), result.getMessage());
        }

        if (!Boolean.TRUE.equals(result.getData())) {
            throw new BizException(ErrorCode.PROJECT_NOT_FOUND);
        }
    }

    private void checkRepositoryExists(Long repositoryId) {
        Result<Boolean> result;

        try {
            result = codeRepositoryClient.existsById(repositoryId);
        } catch (Exception e) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "调用代码仓库服务失败");
        }

        if (result == null || result.getCode() == null) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "代码仓库服务返回为空");
        }

        if (result.getCode() != 0) {
            throw new BizException(result.getCode(), result.getMessage());
        }

        if (!Boolean.TRUE.equals(result.getData())) {
            throw new BizException(ErrorCode.REPOSITORY_NOT_FOUND);
        }
    }

    private void validateEnv(String env) {
        if (!PipelineEnv.isValid(env)) {
            throw new BizException(ErrorCode.PIPELINE_RUN_ENV_INVALID);
        }
    }

    private void validateTriggerType(String triggerType) {
        if (!PipelineTriggerType.isValid(triggerType)) {
            throw new BizException(ErrorCode.PIPELINE_TRIGGER_TYPE_INVALID);
        }
    }

    private PipelineRun getPipelineRunOrThrow(Long id) {
        PipelineRun run = pipelineRunMapper.selectById(id);
        if (run == null) {
            throw new BizException(ErrorCode.PIPELINE_RUN_NOT_FOUND);
        }
        return run;
    }

    private String generateRunNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "RUN-" + date + "-" + random;
    }

    private PipelineRunVO toVO(PipelineRun run) {
        PipelineRunVO vo = new PipelineRunVO();
        vo.setId(run.getId());
        vo.setProjectId(run.getProjectId());
        vo.setRepositoryId(run.getRepositoryId());
        vo.setVersionId(run.getVersionId());
        vo.setRunNo(run.getRunNo());
        vo.setEnv(run.getEnv());
        vo.setStatus(run.getStatus());
        vo.setImageTag(run.getImageTag());
        vo.setCommitHash(run.getCommitHash());
        vo.setTriggerUserId(run.getTriggerUserId());
        vo.setTriggerType(run.getTriggerType());
        vo.setStartedAt(run.getStartedAt());
        vo.setFinishedAt(run.getFinishedAt());
        vo.setDurationSeconds(run.getDurationSeconds());
        vo.setLogText(run.getLogText());
        vo.setErrorMessage(run.getErrorMessage());
        vo.setCreatedAt(run.getCreatedAt());
        vo.setUpdatedAt(run.getUpdatedAt());

        if (PipelineEnv.isValid(run.getEnv())) {
            vo.setEnvDescription(PipelineEnv.valueOf(run.getEnv()).getDescription());
        }

        if (PipelineRunStatus.isValid(run.getStatus())) {
            vo.setStatusDescription(PipelineRunStatus.valueOf(run.getStatus()).getDescription());
        }

        if (PipelineTriggerType.isValid(run.getTriggerType())) {
            vo.setTriggerTypeDescription(PipelineTriggerType.valueOf(run.getTriggerType()).getDescription());
        }

        return vo;
    }
}
