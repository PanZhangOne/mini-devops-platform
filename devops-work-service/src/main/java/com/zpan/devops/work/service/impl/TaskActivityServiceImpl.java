package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.work.entity.TaskActivity;
import com.zpan.devops.work.enums.TaskActivityType;
import com.zpan.devops.work.enums.TaskStatus;
import com.zpan.devops.work.mapper.TaskActivityMapper;
import com.zpan.devops.work.model.request.TaskActivityCreateRequest;
import com.zpan.devops.work.model.vo.TaskActivityVO;
import com.zpan.devops.work.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskActivityMapper taskActivityMapper;

    @Override
    public void create(TaskActivityCreateRequest request) {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTaskId(request.getTaskId());
        taskActivity.setActionType(request.getActionType());
        taskActivity.setActionContent(request.getActionContent());
        taskActivity.setOldValue(request.getOldValue());
        taskActivity.setNewValue(request.getNewValue());
        taskActivity.setCreatedBy(request.getCreatedBy());
        taskActivity.setCreatedAt(LocalDateTime.now());

        taskActivityMapper.insert(taskActivity);
    }

    @Override
    public List<TaskActivityVO> listByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskActivity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskActivity::getTaskId, taskId);
        queryWrapper.orderByDesc(TaskActivity::getCreatedAt);
        queryWrapper.orderByDesc(TaskActivity::getId);

        return taskActivityMapper.selectList(queryWrapper).stream().map(this::toVO).toList();
    }


    private TaskActivityVO toVO(TaskActivity activity) {
        TaskActivityVO vo = new TaskActivityVO();

        vo.setId(activity.getId());
        vo.setTaskId(activity.getTaskId());
        vo.setActionType(activity.getActionType());
        vo.setActionContent(activity.getActionContent());
        vo.setOldValue(activity.getOldValue());
        vo.setNewValue(activity.getNewValue());
        vo.setCreatedBy(activity.getCreatedBy());
        vo.setCreatedAt(activity.getCreatedAt());

        if (TaskActivityType.CHANGE_STATUS.name().equals(activity.getActionType())) {
            vo.setActionTypeDescription(TaskActivityType.CHANGE_STATUS.getDescription());

            if (TaskStatus.isValid(TaskStatus.valueOf(activity.getOldValue()).getDescription())) {
                vo.setOldValueDescription(TaskStatus.valueOf(activity.getOldValue()).getDescription());
            }
            if (TaskStatus.isValid(TaskStatus.valueOf(activity.getNewValue()).getDescription())) {
                vo.setNewValueDescription(TaskStatus.valueOf(activity.getNewValue()).getDescription());
            }
        } else {
            try {
                TaskActivityType type = TaskActivityType.valueOf(activity.getActionType());
                vo.setActionTypeDescription(type.getDescription());
            } catch (Exception ignore) {
                vo.setActionTypeDescription(activity.getActionType());
            }
        }

        return vo;
    }
}
