package com.zpan.devops.pipeline.service;

import com.zpan.devops.pipeline.model.vo.PipelineLogVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface LogStreamService {

    SseEmitter subscribe(Long pipelineRunId);

    void publishLog(Long pipelineRunId, PipelineLogVO logVO);

    void publishComplete(Long pipelineRunId, String status);

    void close(Long pipelineRunId);
}
