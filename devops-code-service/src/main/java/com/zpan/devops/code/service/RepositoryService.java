package com.zpan.devops.code.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpan.devops.code.model.request.RepositoryCreateRequest;
import com.zpan.devops.code.model.request.RepositoryListRequest;
import com.zpan.devops.code.model.request.RepositoryUpdateRequest;
import com.zpan.devops.code.model.vo.CodeRepositoryVO;

public interface RepositoryService {

    CodeRepositoryVO create(RepositoryCreateRequest request, Long currentUserId);

    Page<CodeRepositoryVO> list(RepositoryListRequest request);

    CodeRepositoryVO getById(Long id);

    CodeRepositoryVO update(Long id, RepositoryUpdateRequest request);

    void archive(Long id);

    void delete(Long id);

    boolean existsById(Long id);
}
