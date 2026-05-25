package com.zpan.devops.pipeline.service;

import com.zpan.devops.pipeline.model.request.PipelineStepCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineStepUpdateRequest;
import com.zpan.devops.pipeline.model.vo.PipelineStepVO;

import java.util.List;

public interface PipelineStepService {
    PipelineStepVO create(Long pipelineId, PipelineStepCreateRequest request);

    List<PipelineStepVO> listByPipelineId(Long pipelineId);

    PipelineStepVO getById(Long id);

    PipelineStepVO update(Long id, PipelineStepUpdateRequest request);

    void delete(Long id);
}
