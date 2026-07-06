package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.entity.TaskActivity;
import com.zpan.devops.work.entity.TaskRelation;
import com.zpan.devops.work.enums.TaskActivityType;
import com.zpan.devops.work.enums.TaskRelationType;
import com.zpan.devops.work.mapper.TaskActivityMapper;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.mapper.TaskRelationMapper;
import com.zpan.devops.work.model.request.TaskRelationCreateRequest;
import com.zpan.devops.work.model.vo.TaskRelationVO;
import com.zpan.devops.work.service.TaskRelationService;
import com.zpan.devops.work.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskRelationServiceImpl implements TaskRelationService {

    private final TaskActivityMapper taskActivityMapper;

    private final TaskRelationMapper taskRelationMapper;

    private final TaskService taskService;

    @Override
    public TaskRelationVO create(Long taskId, TaskRelationCreateRequest request, Long currentUserId) {
        Task task = taskService.getTask(taskId);
        validateRelationType(request.getRelationType());
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

        appendLinkActivity(task, relation, currentUserId, now);

        return toVO(relation);
    }

    @Override
    public List<TaskRelationVO> listByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskRelation::getTaskId, taskId);
        wrapper.orderByAsc(TaskRelation::getRelationType);
        wrapper.orderByDesc(TaskRelation::getCreatedAt);
        wrapper.orderByDesc(TaskRelation::getId);

        return taskRelationMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        TaskRelation relation = findOrThrow(id);
        Task task = taskService.getTask(relation.getTaskId());
        taskRelationMapper.deleteById(relation.getId());

        appendUnlinkActivity(task, relation, currentUserId, LocalDateTime.now());
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

    private void validateRelationType(String relationType) {
        if (!TaskRelationType.isValid(relationType)) {
            throw new BizException(ErrorCode.TASK_RELATION_TYPE_INVALID);
        }
    }

    private TaskRelation findOrThrow(Long id) {
        TaskRelation relation = taskRelationMapper.selectById(id);
        if (relation == null) {
            throw new BizException(ErrorCode.TASK_RELATION_NOT_FOUND);
        }
        return relation;
    }

    private void appendLinkActivity(Task task, TaskRelation relation, Long currentUserId, LocalDateTime now) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(task.getId());
        activity.setActionType(resolveLinkActivityType(relation.getRelationType()).name());
        activity.setActionContent(buildLinkContent(relation));
        activity.setOldValue(null);
        activity.setNewValue(relation.getRelationKey());
        activity.setCreatedBy(currentUserId);
        activity.setCreatedAt(now);

        taskActivityMapper.insert(activity);
    }

    private void appendUnlinkActivity(Task task, TaskRelation relation, Long currentUserId, LocalDateTime now) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(task.getId());
        activity.setActionType(TaskActivityType.UNLINK_RELATION.name());
        activity.setActionContent("取消关联：" + buildRelationDisplayText(relation));
        activity.setOldValue(relation.getRelationKey());
        activity.setNewValue(null);
        activity.setCreatedBy(currentUserId);
        activity.setCreatedAt(now);

        taskActivityMapper.insert(activity);
    }

    private TaskActivityType resolveLinkActivityType(String relationType) {
        if (TaskRelationType.BRANCH.name().equals(relationType)) {
            return TaskActivityType.LINK_BRANCH;
        }

        if (TaskRelationType.COMMIT.name().equals(relationType)) {
            return TaskActivityType.LINK_COMMIT;
        }

        if (TaskRelationType.MERGE_REQUEST.name().equals(relationType)) {
            return TaskActivityType.LINK_MERGE_REQUEST;
        }

        if (TaskRelationType.PIPELINE_RUN.name().equals(relationType)) {
            return TaskActivityType.LINK_PIPELINE_RUN;
        }

        if (TaskRelationType.VERSION.name().equals(relationType)) {
            return TaskActivityType.LINK_VERSION;
        }

        if (TaskRelationType.RELEASE.name().equals(relationType)) {
            return TaskActivityType.LINK_RELEASE;
        }

        return TaskActivityType.UNLINK_RELATION;
    }

    private String buildLinkContent(TaskRelation relation) {
        String typeDescription = TaskRelationType.valueOf(relation.getRelationType()).getDescription();
        return "关联" + typeDescription + ": " + buildRelationDisplayText(relation);
    }

    private String buildRelationDisplayText(TaskRelation relation) {
        if (relation.getRelationTitle() != null && !relation.getRelationTitle().isBlank()) {
            return relation.getRelationTitle() + "(" + relation.getRelationKey() + ")";
        }
        return relation.getRelationKey();
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
