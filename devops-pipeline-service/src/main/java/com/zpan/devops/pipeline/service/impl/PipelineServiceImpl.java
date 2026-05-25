package com.zpan.devops.pipeline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import com.zpan.devops.pipeline.client.CodeRepositoryClient;
import com.zpan.devops.pipeline.client.WorkProjectClient;
import com.zpan.devops.pipeline.entity.Pipeline;
import com.zpan.devops.pipeline.entity.PipelineStep;
import com.zpan.devops.pipeline.enums.PipelineStepType;
import com.zpan.devops.pipeline.enums.PipelineTriggerType;
import com.zpan.devops.pipeline.mapper.PipelineMapper;
import com.zpan.devops.pipeline.model.request.PipelineCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineUpdateRequest;
import com.zpan.devops.pipeline.model.vo.PipelineStepVO;
import com.zpan.devops.pipeline.model.vo.PipelineVO;
import com.zpan.devops.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PipelineServiceImpl implements PipelineService {

    private final PipelineMapper pipelineMapper;

    private final WorkProjectClient workProjectClient;

    private final CodeRepositoryClient codeRepositoryClient;


    @Override
    public PipelineVO create(PipelineCreateRequest request, Long currentUserId) {
        validateTriggerType(request.getTriggerType());
        validatePipelineCodeNotExists(request.getProjectId(), request.getCode());
        checkProjectExists(request.getProjectId());
        checkRepositoryExists(request.getRepositoryId());

        LocalDateTime now = LocalDateTime.now();

        Pipeline pipeline = new Pipeline();
        pipeline.setProjectId(request.getProjectId());
        pipeline.setRepositoryId(request.getRepositoryId());
        pipeline.setName(request.getName());
        pipeline.setCode(request.getCode());
        pipeline.setDescription(request.getDescription());
        pipeline.setTriggerType(request.getTriggerType());
        pipeline.setEnabled(true);
        pipeline.setCreatedBy(currentUserId);
        pipeline.setCreatedAt(now);
        pipeline.setUpdatedAt(now);

        pipelineMapper.insert(pipeline);
        return toVO(pipeline);
    }

    @Override
    public List<PipelineVO> list(Long projectId) {
        LambdaQueryWrapper<Pipeline> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(Pipeline::getProjectId, projectId);
        }

        wrapper.orderByDesc(Pipeline::getCreatedAt);
        return pipelineMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public PipelineVO getById(Long id) {
        Pipeline pipeline = getPipelineOrThrow(id);
        return toVO(pipeline);
    }

    @Override
    public PipelineVO update(Long id, PipelineUpdateRequest request) {
        Pipeline pipeline = getPipelineOrThrow(id);
        validateTriggerType(request.getTriggerType());

        pipeline.setName(request.getName());
        pipeline.setDescription(request.getDescription());
        pipeline.setTriggerType(request.getTriggerType());
        pipeline.setEnabled(request.getEnabled());
        pipeline.setUpdatedAt(LocalDateTime.now());

        pipelineMapper.updateById(pipeline);
        return toVO(pipeline);
    }

    @Override
    public void delete(Long id) {
        Pipeline pipeline = getPipelineOrThrow(id);

        LambdaQueryWrapper<Pipeline> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pipeline::getId, pipeline.getId());

        pipelineMapper.delete(wrapper);
    }

    @Override
    public void checkPipelineExists(Long id) {
        getPipelineOrThrow(id);
    }

    @Override
    public Pipeline getEntityById(Long id) {
        return getPipelineOrThrow(id);
    }

    private Pipeline getPipelineOrThrow(Long id) {
        Pipeline pipeline = pipelineMapper.selectById(id);
        if (pipeline == null) {
            throw new BizException(ErrorCode.PIPELINE_NOT_FOUND);
        }
        return pipeline;
    }

    private void checkProjectExists(Long projectId) {
        Result<Boolean> hasProjectResult = workProjectClient.existsById(projectId);

        if (hasProjectResult == null || hasProjectResult.getCode() == null) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "项目服务调用失败" + projectId);
        }
        if (hasProjectResult.getCode() != 0) {
            throw new BizException(hasProjectResult.getCode(), hasProjectResult.getMessage());
        }

        if (!hasProjectResult.getData()) {
            throw new BizException(ErrorCode.PROJECT_NOT_FOUND);
        }
    }

    private void checkRepositoryExists(Long repositoryId) {
        Result<Boolean> hasRepositoryResult = codeRepositoryClient.existsById(repositoryId);

        if (hasRepositoryResult == null || hasRepositoryResult.getCode() == null) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "代码仓库服务调用失败" + repositoryId);
        }
        if (hasRepositoryResult.getCode() != 0) {
            throw new BizException(hasRepositoryResult.getCode(), hasRepositoryResult.getMessage());
        }

        if (!hasRepositoryResult.getData()) {
            throw new BizException(ErrorCode.REPOSITORY_NOT_FOUND);
        }
    }

    private void validateTriggerType(String triggerType) {
        if (!PipelineTriggerType.isValid(triggerType)) {
            throw new BizException(ErrorCode.PIPELINE_TRIGGER_TYPE_INVALID);
        }
    }

    private void validatePipelineCodeNotExists(Long projectId, String pipelineCode) {
        LambdaQueryWrapper<Pipeline> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Pipeline::getProjectId, projectId);
        wrapper.eq(Pipeline::getCode, pipelineCode);
        if (pipelineMapper.selectCount(wrapper) > 0) {
            throw new BizException(ErrorCode.PIPELINE_CODE_EXISTS);
        }
    }

    private PipelineVO toVO(Pipeline pipeline) {
        PipelineVO vo = new PipelineVO();
        vo.setId(pipeline.getId());
        vo.setProjectId(pipeline.getProjectId());
        vo.setRepositoryId(pipeline.getRepositoryId());
        vo.setName(pipeline.getName());
        vo.setCode(pipeline.getCode());
        vo.setDescription(pipeline.getDescription());
        vo.setTriggerType(pipeline.getTriggerType());
        vo.setEnabled(pipeline.getEnabled());
        vo.setCreatedBy(pipeline.getCreatedBy());
        vo.setCreatedAt(pipeline.getCreatedAt());
        vo.setUpdatedAt(pipeline.getUpdatedAt());

        if (PipelineTriggerType.isValid(pipeline.getTriggerType())) {
            vo.setTriggerTypeDescription(PipelineTriggerType.valueOf(pipeline.getTriggerType()).getDescription());
        }

        return vo;
    }

}
