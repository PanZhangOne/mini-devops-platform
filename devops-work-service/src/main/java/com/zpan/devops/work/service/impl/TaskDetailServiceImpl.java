package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.enums.TaskPriority;
import com.zpan.devops.work.enums.TaskStatus;
import com.zpan.devops.work.enums.TaskType;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.model.vo.*;
import com.zpan.devops.work.service.TaskActivityService;
import com.zpan.devops.work.service.TaskCommentService;
import com.zpan.devops.work.service.TaskDetailService;
import com.zpan.devops.work.service.TaskPropertyValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskDetailServiceImpl implements TaskDetailService {

    private final TaskMapper taskMapper;

    private final TaskPropertyValueService taskPropertyValueService;

    private final TaskCommentService taskCommentService;

    private final TaskActivityService taskActivityService;

    @Override
    public TaskDetailVO getDetail(Long taskId) {
        Task task =  getTaskOrThrow(taskId);
        TaskDetailVO detail = toDetailVO(task);
        detail.setChildren(listChildren(taskId));
        detail.setPropertyValues(listPropertyValues(taskId));
        detail.setComments(listComments(taskId));
        detail.setActivities(listActivities(taskId));

        return detail;
    }

    private List<TaskVO> listChildren(Long taskId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getParentTaskId, taskId);
        wrapper.orderByAsc(Task::getSortOrder);
        wrapper.orderByAsc(Task::getId);

        return taskMapper.selectList(wrapper).stream().map(this::toTaskVO).toList();
    }

    private TaskVO toTaskVO(Task task) {
        TaskVO vo = new TaskVO();
        fillTaskVO(vo, task);
        return vo;
    }

    private Task getTaskOrThrow(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private List<TaskPropertyValueVO> listPropertyValues(Long taskId) {
        return taskPropertyValueService.listByTaskId(taskId);
    }

    private List<TaskCommentTreeVO>  listComments(Long taskId) {
        return taskCommentService.treeByTaskId(taskId);
    }

    private List<TaskActivityVO> listActivities(Long taskId) {
        return taskActivityService.listByTaskId(taskId);
    }

    private TaskDetailVO toDetailVO(Task task) {
        TaskDetailVO vo = new TaskDetailVO();
        fillTaskVO(vo, task);
        return vo;
    }

    private void fillTaskVO(TaskVO vo, Task task) {
        vo.setId(task.getId());
        vo.setProjectId(task.getProjectId());
        vo.setModuleId(task.getModuleId());
        vo.setParentTaskId(task.getParentTaskId());
        vo.setTaskNo(task.getTaskNo());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setTaskType(task.getTaskType());
        vo.setStatus(task.getStatus());
        vo.setPriority(task.getPriority());
        vo.setAssigneeId(task.getAssigneeId());
        vo.setReporterId(task.getReporterId());
        vo.setStartedAt(task.getStartedAt());
        vo.setFinishedAt(task.getFinishedAt());
        vo.setEstimatedHours(task.getEstimatedHours());
        vo.setActualHours(task.getActualHours());
        vo.setSortOrder(task.getSortOrder());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());
        if (TaskType.isValid(task.getTaskType())) {
            vo.setTaskTypeDescription(TaskType.valueOf(task.getTaskType()).getDescription());
        }

        if (TaskStatus.isValid(task.getStatus())) {
            vo.setStatusDescription(TaskStatus.valueOf(task.getStatus()).getDescription());
        }

        if (TaskPriority.isValid(task.getPriority())) {
            vo.setPriorityDescription(TaskPriority.valueOf(task.getPriority()).getDescription());
        }
    }
}
