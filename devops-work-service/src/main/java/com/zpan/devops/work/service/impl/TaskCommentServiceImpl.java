package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.entity.TaskActivity;
import com.zpan.devops.work.entity.TaskComment;
import com.zpan.devops.work.enums.TaskActivityType;
import com.zpan.devops.work.mapper.TaskActivityMapper;
import com.zpan.devops.work.mapper.TaskCommentMapper;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.model.request.TaskCommentCreateRequest;
import com.zpan.devops.work.model.vo.TaskCommentTreeVO;
import com.zpan.devops.work.model.vo.TaskCommentVO;
import com.zpan.devops.work.service.TaskCommentService;
import com.zpan.devops.work.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskService taskService;

    private final TaskCommentMapper taskCommentMapper;

    private final TaskActivityMapper taskActivityMapper;

    @Override
    public TaskCommentVO create(Long taskId, TaskCommentCreateRequest request, Long currentUserId) {
        taskService.getById(taskId);

        if (request.getParentId() != null) {
            validateParentComment(request.getParentId(), taskId);
        }
        LocalDateTime now = LocalDateTime.now();
        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setCreatedBy(currentUserId);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        taskCommentMapper.insert(comment);
        return toVO(comment);
    }

    @Override
    public List<TaskCommentTreeVO> treeByTaskId(Long taskId) {
        taskService.getById(taskId);

        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId);
        wrapper.orderByAsc(TaskComment::getCreatedAt);
        wrapper.orderByAsc(TaskComment::getId);

        List<TaskCommentTreeVO> nodes = taskCommentMapper.selectList(wrapper)
                .stream().map(this::toTreeVO).toList();
        Map<Long, TaskCommentTreeVO> nodeMap = nodes.stream()
                .collect(Collectors.toMap(TaskCommentTreeVO::getId, item -> item));
        List<TaskCommentTreeVO> roots = nodes.stream()
                .filter(item -> item.getParentId() == null || !nodeMap.containsKey(item.getParentId()))
                .collect(Collectors.toCollection(ArrayList::new));
        for (TaskCommentTreeVO node : nodes) {
            Long parentId = node.getParentId();
            if (parentId == null) {
                continue;
            }
            TaskCommentTreeVO parent = nodeMap.get(parentId);
            if (parent != null) {
                parent.getChildren().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        TaskComment comment = findOrThrow(id);
        taskCommentMapper.deleteById(comment.getId());
    }

    private TaskComment findOrThrow(Long id) {
        TaskComment comment = taskCommentMapper.selectById(id);
        if (comment == null) {
            throw new BizException(ErrorCode.TASK_COMMENT_NOT_FOUND);
        }
        return comment;
    }

    private void validateParentComment(Long parentId, Long taskId) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getId, parentId);
        wrapper.eq(TaskComment::getTaskId, taskId);

        TaskComment comment = taskCommentMapper.selectOne(wrapper);

        if (comment == null) {
            throw new BizException(ErrorCode.TASK_COMMENT_NOT_FOUND);
        }
    }

    private void appendAddCommentActivity(Task task, TaskComment comment
            , Long currentUserId, LocalDateTime now) {
        String content = comment.getParentId() == null ? "添加评论" : "回复评论";
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(task.getId());
        activity.setActionType(TaskActivityType.ADD_COMMENT.name());
        activity.setActionContent(content + "：" + abbreviate(comment.getContent(), 100));
        activity.setOldValue(null);
        activity.setNewValue(String.valueOf(comment.getId()));
        activity.setCreatedBy(currentUserId);
        activity.setCreatedAt(now);
        taskActivityMapper.insert(activity);
    }

    private String abbreviate(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }

        return content.substring(0, maxLength) + "...";
    }

    private void sortTree(List<TaskCommentTreeVO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(Comparator
                .comparing(TaskCommentTreeVO::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(TaskCommentTreeVO::getId));
        for (TaskCommentTreeVO node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private TaskCommentVO toVO(TaskComment comment) {
        TaskCommentVO vo = new TaskCommentVO();
        vo.setId(comment.getId());
        vo.setTaskId(comment.getTaskId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setCreatedBy(comment.getCreatedBy());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setUpdatedAt(comment.getUpdatedAt());

        return vo;
    }


    private TaskCommentTreeVO toTreeVO(TaskComment comment) {
        TaskCommentTreeVO vo = new TaskCommentTreeVO();
        vo.setId(comment.getId());
        vo.setTaskId(comment.getTaskId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setCreatedBy(comment.getCreatedBy());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setUpdatedAt(comment.getUpdatedAt());
        return vo;
    }


}
