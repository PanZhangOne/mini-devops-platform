package com.zpan.devops.code.service;

import com.zpan.devops.code.model.request.RepositoryCreateRequest;
import com.zpan.devops.code.model.request.RepositoryUpdateRequest;
import com.zpan.devops.code.model.vo.RepositoryVO;

import java.util.List;

public interface RepositoryService {

    RepositoryVO create(RepositoryCreateRequest request);

    List<RepositoryVO> list();

    List<RepositoryVO> listByProjectId(Long projectId);

    RepositoryVO getById(Long id);

    RepositoryVO update(Long id, RepositoryUpdateRequest request);

    void delete(Long id);

    boolean existsById(Long id);
}
