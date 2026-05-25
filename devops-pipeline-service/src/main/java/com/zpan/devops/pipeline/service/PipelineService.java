package com.zpan.devops.pipeline.service;

import com.zpan.devops.pipeline.entity.Pipeline;
import com.zpan.devops.pipeline.model.request.PipelineCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineUpdateRequest;
import com.zpan.devops.pipeline.model.vo.PipelineVO;

import java.util.List;

public interface PipelineService {
    PipelineVO create(PipelineCreateRequest request, Long currentUserId);

    List<PipelineVO> list(Long projectId);

    PipelineVO getById(Long id);

    PipelineVO update(Long id, PipelineUpdateRequest request);

    void delete(Long id);

    void checkPipelineExists(Long id);

    Pipeline getEntityById(Long id);
}
