package com.zpan.devops.pipeline.service;

import com.zpan.devops.pipeline.model.request.PipelineRunCreateRequest;
import com.zpan.devops.pipeline.model.request.PipelineRunQueryRequest;
import com.zpan.devops.pipeline.model.request.RunPipelineRequest;
import com.zpan.devops.pipeline.model.vo.*;

import java.util.List;

public interface PipelineRunService {

    PipelineRunVO create(Long pipelineId, PipelineRunCreateRequest request, Long currentUserId);

    List<PipelineRunVO> list(PipelineRunQueryRequest query);

    PipelineRunVO getById(Long id);

    List<PipelineStepRunVO> listSteps(Long pipelineRunId);

    List<PipelineLogVO> listLogs(Long pipelineRunId);
}
