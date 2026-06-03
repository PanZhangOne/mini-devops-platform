package com.zpan.devops.pipeline.service;

import com.zpan.devops.pipeline.model.request.CredentialCreateRequest;
import com.zpan.devops.pipeline.model.request.CredentialUpdateRequest;
import com.zpan.devops.pipeline.model.vo.CredentialSecretVO;
import com.zpan.devops.pipeline.model.vo.CredentialVO;

import java.util.List;

public interface CredentialService {
    CredentialVO create(CredentialCreateRequest request, Long currentUserId);

    List<CredentialVO> list(Long projectId);

    CredentialVO getById(Long id);

    CredentialVO update(Long id, CredentialUpdateRequest request);

    void delete(Long id);

    CredentialSecretVO getSecretById(Long id);
}
