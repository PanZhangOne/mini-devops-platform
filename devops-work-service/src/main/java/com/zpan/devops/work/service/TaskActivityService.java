package com.zpan.devops.work.service;

import com.zpan.devops.work.model.request.TaskActivityCreateRequest;
import com.zpan.devops.work.model.vo.TaskActivityVO;

import java.util.List;

public interface TaskActivityService {

    void create(TaskActivityCreateRequest request);

    List<TaskActivityVO> listByTaskId(Long taskId);
}
