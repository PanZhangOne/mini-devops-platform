package com.zpan.devops.work.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.model.request.*;
import com.zpan.devops.work.model.vo.ProjectTaskStatsVO;
import com.zpan.devops.work.model.vo.TaskVO;

import java.util.List;

public interface TaskService {


    TaskVO create(TaskCreateRequest request);

    List<TaskVO> list();

    List<TaskVO> listByProjectId(Long projectId);

    Page<TaskVO> listByProjectId(Long projectId, TaskListRequest request);

    TaskVO getById(Long id);

    TaskVO update(Long id, TaskUpdateRequest request);

    Task getTask(Long id);

    /**
     *
     * @param id
     * @param request
     * @deprecated 已经不推荐使用
     */
    TaskVO updateStatus(Long id, TaskStatusUpdateRequest request);

    void delete(Long id);

    void changeStatus(Long id, TaskStatusChangeRequest request);

    ProjectTaskStatsVO getProjectTaskStats(Long projectId);

    boolean existsByProjectId(Long projectId);

    boolean existsById(Long id);
}
