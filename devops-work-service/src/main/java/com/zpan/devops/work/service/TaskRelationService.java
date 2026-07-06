package com.zpan.devops.work.service;


import com.zpan.devops.work.model.request.TaskRelationCreateRequest;
import com.zpan.devops.work.model.vo.TaskRelationVO;

import java.util.List;

public interface TaskRelationService {

    TaskRelationVO create(Long taskId, TaskRelationCreateRequest request, Long currentUserId);

    List<TaskRelationVO> listByTaskId(Long taskId);

    void delete(Long id, Long currentUserId);
}
