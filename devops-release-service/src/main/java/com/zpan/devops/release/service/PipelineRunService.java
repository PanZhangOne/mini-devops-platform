package com.zpan.devops.release.service;

import com.zpan.devops.release.model.request.PipelineRunCreateRequest;
import com.zpan.devops.release.model.request.PipelineRunQueryRequest;
import com.zpan.devops.release.model.request.PipelineRunStatusUpdateRequest;
import com.zpan.devops.release.model.vo.PipelineRunVO;

import java.util.List;

public interface PipelineRunService {

    PipelineRunVO create(PipelineRunCreateRequest request, Long currentUserId);

    List<PipelineRunVO> list(PipelineRunQueryRequest query);

    PipelineRunVO getById(Long id);

    PipelineRunVO updateStatus(Long id, PipelineRunStatusUpdateRequest request);

    void delete(Long id);
}
