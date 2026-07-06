package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpan.devops.common.config.IdGeneratorConfig;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.util.SnowflakeIdWorker;
import com.zpan.devops.work.domain.TaskStatusTransition;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.enums.TaskActivityType;
import com.zpan.devops.work.enums.TaskPriority;
import com.zpan.devops.work.enums.TaskStatus;
import com.zpan.devops.work.event.TaskEvent;
import com.zpan.devops.work.event.TaskEventFactory;
import com.zpan.devops.work.event.TaskEventProducer;
import com.zpan.devops.work.event.TaskEventType;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.model.request.*;
import com.zpan.devops.work.model.vo.ProjectTaskStatsVO;
import com.zpan.devops.work.model.vo.TaskVO;
import com.zpan.devops.work.service.EventMessageService;
import com.zpan.devops.work.service.ProjectService;
import com.zpan.devops.work.service.TaskActivityService;
import com.zpan.devops.work.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerMapping;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {


    private final TaskMapper taskMapper;

    private final ProjectService projectService;

    private final TaskActivityService taskActivityService;

    private final SnowflakeIdWorker snowflakeIdWorker;

    private final TaskStatusTransition taskStatusTransition;

    private final TaskEventFactory taskEventFactory;

    private final TaskEventProducer taskEventProducer;

    private final EventMessageService eventMessageService;

    @Override
    public TaskVO create(TaskCreateRequest request) {
        projectService.checkProjectExist(request.getProjectId());

        if (!TaskPriority.isValid(request.getPriority())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "任务优先级不合法");
        }
        LocalDateTime now = LocalDateTime.now();

        Task task = new Task();
        task.setProjectId(request.getProjectId());
        task.setTaskNo(String.valueOf(snowflakeIdWorker.nextId()));
        task.setTitle(request.getTitle());
        task.setParentTaskId(request.getParentId());
        task.setModuleId(request.getModuleId());
        task.setTaskType(request.getTaskType());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        task.setStatus(TaskStatus.TODO.name());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setActualHours(request.getActualHours());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setAssigneeId(request.getAssigneeId());
        task.setReporterId(request.getReporterId());
        task.setSortOrder(request.getSortOrder());
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
    public Page<TaskVO> listByProjectId(Long projectId, TaskListRequest request) {
        var exists = existsByProjectId(projectId);
        if (!exists) {
            throw new BizException(ErrorCode.PROJECT_NOT_FOUND);
        }

        int pageNO = request.getPageNo();
        int pageSize = request.getPageSize();
        pageSize = Math.min(pageNO, 100);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);

        if (request.getModuleId() != null) {
            wrapper.eq(Task::getModuleId, request.getModuleId());
        }
        if (request.getParentTaskId() != null) {
            wrapper.eq(Task::getParentTaskId, request.getParentTaskId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Task::getStatus, request.getStatus());
        }
        if (request.getTaskType() != null) {
            wrapper.eq(Task::getTaskType, request.getTaskType());
        }
        if (request.getPriority() != null) {
            wrapper.eq(Task::getPriority, request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            wrapper.eq(Task::getAssigneeId, request.getAssigneeId());
        }
        if (request.getKeyword() != null) {
            wrapper.and(w ->
                    w.like(Task::getTitle, request.getKeyword())
                            .or().like(Task::getTaskNo, request.getKeyword()));
        }

        wrapper.orderByAsc(Task::getSortOrder);
        wrapper.orderByDesc(Task::getCreatedAt);

        Page<Task> taskPage = taskMapper.selectPage(new Page<>(pageNO, pageSize), wrapper);
        Page<TaskVO> voPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        voPage.setRecords(taskPage.getRecords().stream().map(this::toVO).toList());

        return voPage;
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
        task.setModuleId(request.getModuleId());
        task.setParentTaskId(request.getParentTaskId());
        task.setTaskType(request.getTaskType());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        task.setReporterId(request.getReporterId());
        task.setPriority(request.getPriority());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setActualHours(request.getActualHours());
        task.setSortOrder(request.getSortOrder());

        task.setDueDate(request.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());

        taskMapper.updateById(task);
        return toVO(task);
    }

    @Override
    public Task getTask(Long id) {
        return getTaskOrThrow(id);
    }

    @Override
    public TaskVO updateStatus(Long id, TaskStatusUpdateRequest request) {
        Task task = getTaskOrThrow(id);
        if (!TaskStatus.isValid(request.getStatus())) {
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
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, TaskStatusChangeRequest request) {
        Task task = getTaskOrThrow(id);

        var canTransit = taskStatusTransition.canTransit(task.getStatus(), request.getTargetStatus());
        if (!canTransit) {
            throw new BizException(ErrorCode.TASK_STATUS_TRANSITION_INVALID);
        }

        LocalDateTime now = LocalDateTime.now();
        String oldStatus = task.getStatus();

        task.setStatus(request.getTargetStatus());

        if (TaskStatus.IN_PROGRESS.name().equals(request.getTargetStatus()) && task.getStartedAt() == null) {
            task.setStartedAt(now);
        }
        if (TaskStatus.DONE.name().equals(request.getTargetStatus()) || TaskStatus.CANCELLED.name().equals(request.getTargetStatus())) {
            task.setFinishedAt(now);
        }
        if (TaskStatus.IN_PROGRESS.name().equals(request.getTargetStatus())) {
            task.setFinishedAt(null);
        }


        task.setUpdatedAt(now);

        // 更新数据
        taskMapper.updateById(task);
        // 写入日志
        appendStatusChangeActivity(task, oldStatus, request.getTargetStatus(), request.getRemark(), request.getUserId(), now);

        // 增加消息队列
        TaskEvent event = taskEventFactory.statusChanged(task, oldStatus, request.getTargetStatus(), request.getRemark(), request.getUserId());
        eventMessageService.saveTaskEvent(event);
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

    @Override
    public boolean existsById(Long id) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getId, id);
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
        vo.setDueDate(task.getDueDate());
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

    private void appendStatusChangeActivity(
            Task task,
            String oldStatus,
            String newStatus,
            String remark,
            Long currentUserId,
            LocalDateTime now
    ) {
        String oldDescription = TaskStatus.valueOf(oldStatus).getDescription();
        String newDescription = TaskStatus.valueOf(newStatus).getDescription();

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder
                .append("任务状态从 ").append(oldDescription)
                .append(" 变更为 ")
                .append(newDescription);

        if (remark != null && !remark.isBlank()) {
            contentBuilder.append(", 备注: ").append(remark);
        }

        TaskActivityCreateRequest request = new TaskActivityCreateRequest();
        request.setTaskId(task.getId());
        request.setActionType(TaskActivityType.CHANGE_STATUS.name());
        request.setActionContent(contentBuilder.toString());
        request.setOldValue(oldStatus);
        request.setNewValue(newStatus);
        request.setCreatedBy(currentUserId);
        request.setCreatedAt(now);

        taskActivityService.create(request);
    }
}
