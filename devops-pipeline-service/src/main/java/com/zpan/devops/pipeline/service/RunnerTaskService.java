package com.zpan.devops.pipeline.service;

import com.zpan.devops.pipeline.model.request.*;
import com.zpan.devops.pipeline.model.vo.RunnerTaskVO;

public interface RunnerTaskService {
    RunnerTaskVO fetchNext(RunnerTaskNextRequest request);

    void startStep(Long pipelineRunId, Long stepRunId, RunnerTaskStepStartRequest request);

    void finishStep(Long pipelineRunId, Long stepRunId, RunnerTaskStepFinishRequest request);

    void appendLog(Long pipelineRunId, RunnerTaskLogAppendRequest request);

    void finishRun(Long pipelineRunId, RunnerTaskFinishRequest request);

    void updateWorkspace(Long pipelineRunId, RunnerTaskWorkspaceUpdateRequest request);
}
