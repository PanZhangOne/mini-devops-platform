package com.zpan.devops.work.service;

import com.zpan.devops.work.model.request.ProjectModuleCreateRequest;
import com.zpan.devops.work.model.request.ProjectModuleUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectModuleTreeVO;
import com.zpan.devops.work.model.vo.ProjectModuleVO;

import java.util.List;

public interface ProjectModuleService {

    ProjectModuleVO create(Long projectId, ProjectModuleCreateRequest request, Long currentUserId);

    List<ProjectModuleVO> listByProjectId(Long projectId);

    List<ProjectModuleTreeVO> treeByProjectId(Long projectId);

    ProjectModuleVO getById(Long id);

    ProjectModuleVO update(Long id, ProjectModuleUpdateRequest request);

    void delete(Long id);
}
