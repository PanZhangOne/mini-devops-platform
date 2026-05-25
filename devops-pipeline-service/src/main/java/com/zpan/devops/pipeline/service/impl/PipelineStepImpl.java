package com.zpan.devops.pipeline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.pipeline.entity.Pipeline;
import com.zpan.devops.pipeline.entity.PipelineStep;
import com.zpan.devops.pipeline.enums.PipelineStepType;
import com.zpan.devops.pipeline.mapper.PipelineMapper;
import com.zpan.devops.pipeline.mapper.PipelineStepMapper;
import com.zpan.devops.pipeline.model.request.PipelineStepCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineStepUpdateRequest;
import com.zpan.devops.pipeline.model.vo.PipelineStepVO;
import com.zpan.devops.pipeline.service.PipelineService;
import com.zpan.devops.pipeline.service.PipelineStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PipelineStepImpl implements PipelineStepService {

    private final PipelineStepMapper pipelineStepMapper;

    private final PipelineService pipelineService;

    @Override
    public PipelineStepVO create(Long pipelineId, PipelineStepCreateRequest request) {
        pipelineService.checkPipelineExists(pipelineId);
        PipelineStep pipelineStep = new PipelineStep();

        LocalDateTime now = LocalDateTime.now();

        pipelineStep.setPipelineId(pipelineId);
        pipelineStep.setName(request.getName());
        pipelineStep.setStepType(request.getStepType());
        pipelineStep.setSortOrder(request.getSortOrder());
        pipelineStep.setCommand(request.getCommand());
        pipelineStep.setConfigJson(request.getConfigJson());
        pipelineStep.setEnabled(request.getEnabled());
        pipelineStep.setCreatedAt(now);
        pipelineStep.setUpdatedAt(now);

        pipelineStepMapper.insert(pipelineStep);
        return toVO(pipelineStep);
    }

    @Override
    public List<PipelineStepVO> listByPipelineId(Long pipelineId) {
        pipelineService.checkPipelineExists(pipelineId);

        LambdaQueryWrapper<PipelineStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineStep::getPipelineId, pipelineId);
        wrapper.orderByAsc(PipelineStep::getSortOrder);

        return pipelineStepMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public PipelineStepVO getById(Long id) {
        PipelineStep pipelineStep = getStepOrThrow(id);
        return toVO(pipelineStep);
    }

    @Override
    public PipelineStepVO update(Long stepId, PipelineStepUpdateRequest request) {
        validateStepType(request.getStepType());
        PipelineStep step = getStepOrThrow(stepId);
        validateSortOrderNotExists(step.getPipelineId(), request.getSortOrder(), stepId);

        step.setName(request.getName());
        step.setStepType(request.getStepType());
        step.setSortOrder(request.getSortOrder());
        step.setCommand(request.getCommand());
        step.setConfigJson(request.getConfigJson());
        step.setEnabled(request.getEnabled() == null ? step.getEnabled() : request.getEnabled());
        step.setUpdatedAt(LocalDateTime.now());

        pipelineStepMapper.updateById(step);
        return toVO(step);
    }

    @Override
    public void delete(Long stepId) {
        PipelineStep pipelineStep = getStepOrThrow(stepId);
        pipelineStepMapper.deleteById(pipelineStep.getId());
    }

    private PipelineStep getStepOrThrow(Long pipelineStepId) {
        PipelineStep step = pipelineStepMapper.selectById(pipelineStepId);
        if (step == null) {
            throw new BizException(ErrorCode.PIPELINE_STEP_NOT_FOUND);
        }
        return step;
    }

    private void validateStepType(String stepType) {
        if (!PipelineStepType.isValid(stepType)) {
            throw new BizException(ErrorCode.PIPELINE_STEP_TYPE_INVALID);
        }
    }

    private void validateSortOrderNotExists(Long pipelineId, Integer sortOrder, Long excludeId) {
        LambdaQueryWrapper<PipelineStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PipelineStep::getPipelineId, pipelineId);
        wrapper.eq(PipelineStep::getSortOrder, sortOrder);

        if (excludeId != null) {
            wrapper.ne(PipelineStep::getId, excludeId);
        }

        Long count = pipelineStepMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ErrorCode.PIPELINE_STEP_ORDER_EXISTS);
        }
    }

    private PipelineStepVO toVO(PipelineStep step) {
        PipelineStepVO vo = new PipelineStepVO();
        vo.setId(step.getId());
        vo.setPipelineId(step.getPipelineId());
        vo.setName(step.getName());
        vo.setStepType(step.getStepType());
        vo.setSortOrder(step.getSortOrder());
        vo.setCommand(step.getCommand());
        vo.setConfigJson(step.getConfigJson());
        vo.setEnabled(step.getEnabled());
        vo.setCreatedAt(step.getCreatedAt());
        vo.setUpdatedAt(step.getUpdatedAt());

        if (PipelineStepType.isValid(step.getStepType())) {
            vo.setStepTypeDescription(PipelineStepType.valueOf(step.getStepType()).getDescription());
        }

        return vo;
    }
}
