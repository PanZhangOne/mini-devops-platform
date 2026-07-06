package com.zpan.devops.work.service;

import com.zpan.devops.work.model.vo.TaskDetailVO;

public interface TaskDetailService {

    TaskDetailVO getDetail(Long taskId);
}
