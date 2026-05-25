package com.zpan.devops.work.service;

import com.zpan.devops.work.model.request.TaskCreateRequest;
import com.zpan.devops.work.model.request.TaskStatusUpdateRequest;
import com.zpan.devops.work.model.request.TaskUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectTaskStatsVO;
import com.zpan.devops.work.model.vo.TaskVO;

import java.util.List;

public interface TaskService {


    TaskVO create(TaskCreateRequest request);

    List<TaskVO> list();

    List<TaskVO> listByProjectId(Long projectId);

    TaskVO getById(Long id);

    TaskVO update(Long id, TaskUpdateRequest request);

    TaskVO updateStatus(Long id, TaskStatusUpdateRequest request);

    void delete(Long id);

    ProjectTaskStatsVO getProjectTaskStats(Long projectId);

    boolean existsByProjectId(Long projectId);
}
