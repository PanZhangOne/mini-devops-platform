package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.entity.TaskRelation;
import com.zpan.devops.work.enums.TaskRelationType;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.mapper.TaskRelationMapper;
import com.zpan.devops.work.model.request.TaskRelationCreateRequest;
import com.zpan.devops.work.model.vo.TaskRelationVO;
import com.zpan.devops.work.service.TaskRelationService;
import com.zpan.devops.work.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskRelationServiceImpl implements TaskRelationService {

    private TaskRelationMapper taskRelationMapper;

    private TaskService taskService;

    @Override
    public TaskRelationVO create(Long taskId, TaskRelationCreateRequest request, Long currentUserId) {
        if (!taskService.existsById(taskId)) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        checkTaskRelationExits(taskId, request);

        LocalDateTime now = LocalDateTime.now();
        TaskRelation relation = new TaskRelation();
        relation.setTaskId(taskId);
        relation.setRelationId(request.getRelationId());
        relation.setRelationKey(request.getRelationKey());
        relation.setRelationType(request.getRelationType());
        relation.setRelationTitle(request.getRelationTitle());
        relation.setCreatedBy(currentUserId);
        relation.setCreatedAt(now);

        taskRelationMapper.insert(relation);
        return toVO(relation);
    }

    @Override
    public List<TaskRelationVO> listByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskRelation::getTaskId, taskId);
        wrapper.orderByAsc(TaskRelation::getCreatedAt);
        return taskRelationMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        TaskRelation relation = findOrThrow(id);
        Task task =  taskService.getById(relation.getTaskId());
        taskRelationMapper.deleteById(relation.getId());

        appendUnlinkActivity()
    }

    private void checkTaskRelationExits(Long taskId, TaskRelationCreateRequest request) {
        LambdaQueryWrapper<TaskRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskRelation::getTaskId, taskId);
        wrapper.eq(TaskRelation::getRelationType, request.getRelationType());
        wrapper.eq(TaskRelation::getRelationId, request.getRelationId());

        if (taskRelationMapper.selectCount(wrapper) > 0) {
            throw new BizException(ErrorCode.TASK_RELATION_EXISTS);
        }
    }

    private TaskRelation findOrThrow(Long id) {
        TaskRelation relation = taskRelationMapper.selectById(id);
        if (relation == null) {
            throw new BizException(ErrorCode.TASK_RELATION_NOT_FOUND);
        }
        return relation;
    }

    private TaskRelationVO toVO(TaskRelation relation) {
        TaskRelationVO vo = new TaskRelationVO();
        vo.setId(relation.getId());
        vo.setTaskId(relation.getTaskId());
        vo.setCreatedAt(relation.getCreatedAt());
        vo.setRelationType(relation.getRelationType());
        vo.setRelationId(relation.getRelationId());
        vo.setRelationKey(relation.getRelationKey());
        vo.setRelationTitle(relation.getRelationTitle());

        if (vo.getRelationType() != null && TaskRelationType.isValid(vo.getRelationType())) {
            vo.setRelationTypeDescription(TaskRelationType.getDescription(vo.getRelationType()));
        }

        return vo;
    }
}
