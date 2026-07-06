package com.zpan.devops.work.service;

import com.zpan.devops.work.model.request.TaskPropertyValueSaveRequest;
import com.zpan.devops.work.model.vo.TaskPropertyValueVO;

import java.util.List;

public interface TaskPropertyValueService {

    void saveValues(Long taskId, TaskPropertyValueSaveRequest request);

    List<TaskPropertyValueVO> listByTaskId(Long taskId);
}
