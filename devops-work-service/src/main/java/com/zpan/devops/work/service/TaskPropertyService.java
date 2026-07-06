package com.zpan.devops.work.service;

import com.zpan.devops.work.model.request.TaskPropertyCreateRequest;
import com.zpan.devops.work.model.request.TaskPropertyUpdateRequest;
import com.zpan.devops.work.model.vo.TaskPropertyVO;

import java.util.List;

public interface TaskPropertyService {

    TaskPropertyVO create(Long projectId, TaskPropertyCreateRequest request, Long currentUserId);

    List<TaskPropertyVO> listByProjectId(Long projectId);

    TaskPropertyVO getById(Long id);

    TaskPropertyVO update(Long id, TaskPropertyUpdateRequest request);

    void delete(Long id);
}
