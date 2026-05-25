package com.zpan.devops.pipeline.service;

import com.zpan.devops.pipeline.model.request.RunnerHeartbeatRequest;
import com.zpan.devops.pipeline.model.request.RunnerRegisterRequest;
import com.zpan.devops.pipeline.model.vo.RunnerVO;

import java.util.List;

public interface RunnerService {
    RunnerVO register(RunnerRegisterRequest request);

    RunnerVO heartbeat(RunnerHeartbeatRequest request);

    List<RunnerVO> list();

    RunnerVO getById(Long id);

    RunnerVO updateStatus(Long id, String status);
}
