package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.enums.TaskPriority;
import com.zpan.devops.work.enums.TaskStatus;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.model.request.TaskCreateRequest;
import com.zpan.devops.work.model.request.TaskStatusUpdateRequest;
import com.zpan.devops.work.model.request.TaskUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectTaskStatsVO;
import com.zpan.devops.work.model.vo.TaskVO;
import com.zpan.devops.work.service.ProjectService;
import com.zpan.devops.work.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {


    private final TaskMapper taskMapper;

    private final ProjectService projectService;

    @Override
    public TaskVO create(TaskCreateRequest request) {
        projectService.checkProjectExist(request.getProjectId());

        if (!TaskPriority.isValid(request.getPriority())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "任务优先级不合法");
        }
        LocalDateTime now = LocalDateTime.now();

        Task task = new Task();
        task.setProjectId(request.getProjectId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        task.setStatus(TaskStatus.TODO.name());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        taskMapper.insert(task);
        return toVO(task);
    }

    @Override
    public List<TaskVO> list() {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Task::getCreatedAt);

        return taskMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public List<TaskVO> listByProjectId(Long projectId) {
        projectService.checkProjectExist(projectId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);
        wrapper.orderByDesc(Task::getCreatedAt);

        return taskMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public TaskVO getById(Long id) {
        Task task = getTaskOrThrow(id);
        return toVO(task);
    }

    @Override
    public TaskVO update(Long id, TaskUpdateRequest request) {
        Task task = getTaskOrThrow(id);
        projectService.checkProjectExist(task.getProjectId());

        if (TaskStatus.isValid(request.getStatus())) {
            throw new BizException(ErrorCode.TASK_STATUS_INVALID);
        }
        if (TaskPriority.isValid(request.getPriority())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "任务优先级不合法");
        }

        task.setProjectId(request.getProjectId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setUpdatedAt(LocalDateTime.now());

        taskMapper.updateById(task);
        return toVO(task);
    }

    @Override
    public TaskVO updateStatus(Long id, TaskStatusUpdateRequest request) {
        Task task = getTaskOrThrow(id);
        if (TaskStatus.isValid(request.getStatus())) {
            throw new BizException(ErrorCode.TASK_STATUS_INVALID);
        }

        task.setStatus(request.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return toVO(task);
    }

    @Override
    public void delete(Long id) {
        Task task = getTaskOrThrow(id);
        taskMapper.deleteById(task);
    }

    @Override
    public ProjectTaskStatsVO getProjectTaskStats(Long projectId) {
        projectService.checkProjectExist(projectId);

        ProjectTaskStatsVO stats = new ProjectTaskStatsVO();
        stats.setProjectId(projectId);


        long totalCount = countByStatus(projectId, null);
        long todoCount = countByStatus(projectId, TaskStatus.TODO.name());
        long inProgressCount = countByStatus(projectId, TaskStatus.IN_PROGRESS.name());
        long testingCount = countByStatus(projectId, TaskStatus.TESTING.name());
        long doneCount = countByStatus(projectId, TaskStatus.DONE.name());
        long cancelledCount = countByStatus(projectId, TaskStatus.CANCELLED.name());

        stats.setTotalCount(totalCount);
        stats.setTodoCount(todoCount);
        stats.setInProgressCount(inProgressCount);
        stats.setTestingCount(testingCount);
        stats.setDoneCount(doneCount);
        stats.setCancelledCount(cancelledCount);

        if (totalCount == 0) {
            stats.setProgress(0);
        } else {
            stats.setProgress((int) Math.round(doneCount * 100.0 / totalCount));
        }

        return stats;
    }

    @Override
    public boolean existsByProjectId(Long projectId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);
        return taskMapper.selectCount(wrapper) > 0;
    }

    private long countByStatus(Long projectId, String status) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        return taskMapper.selectCount(wrapper);
    }

    private TaskVO toVO(Task task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setProjectId(task.getProjectId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setAssigneeId(task.getAssigneeId());
        vo.setStatus(task.getStatus());
        vo.setPriority(task.getPriority());
        vo.setDeadline(task.getDeadline());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());

        if (TaskStatus.isValid(task.getStatus())) {
            vo.setStatusDescription(TaskStatus.valueOf(task.getStatus()).getDescription());
        }

        if (TaskPriority.isValid(task.getPriority())) {
            vo.setPriorityDescription(TaskPriority.valueOf(task.getPriority()).getDescription());
        }

        return vo;
    }

    private Task getTaskOrThrow(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }
}
