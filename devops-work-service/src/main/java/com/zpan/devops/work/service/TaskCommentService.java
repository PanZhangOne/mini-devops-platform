package com.zpan.devops.work.service;

import com.zpan.devops.work.model.request.TaskCommentCreateRequest;
import com.zpan.devops.work.model.vo.TaskCommentTreeVO;
import com.zpan.devops.work.model.vo.TaskCommentVO;

import java.util.List;

public interface TaskCommentService {

    TaskCommentVO create(Long taskId, TaskCommentCreateRequest request, Long currentUserId);

    List<TaskCommentTreeVO> treeByTaskId(Long taskId);

    void delete(Long id, Long currentUserId);
}
