package com.zpan.devops.work.service;

import com.zpan.devops.work.model.request.ProjectCreateRequest;
import com.zpan.devops.work.model.request.ProjectUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectVO;

import java.util.List;

public interface ProjectService {

    ProjectVO create(ProjectCreateRequest request);

    List<ProjectVO> list();

    ProjectVO getById(Long id);

    ProjectVO update(Long id, ProjectUpdateRequest request);

    void delete(Long id);

    void checkProjectExist(Long id);

    boolean existsById(Long id);
}
