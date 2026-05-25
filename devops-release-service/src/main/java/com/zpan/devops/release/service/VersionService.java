package com.zpan.devops.release.service;

import com.zpan.devops.release.model.request.VersionCreateRequest;
import com.zpan.devops.release.model.request.VersionStatusUpdateRequest;
import com.zpan.devops.release.model.request.VersionUpdateRequest;
import com.zpan.devops.release.model.vo.VersionVO;

import java.util.List;

public interface VersionService {
    VersionVO create(VersionCreateRequest request, Long currentUserId);

    List<VersionVO> list(Long projectId);

    VersionVO getById(Long id);

    VersionVO update(Long id, VersionUpdateRequest request);

    VersionVO updateStatus(Long id, VersionStatusUpdateRequest request);

    void delete(Long id);

    void checkVersionExists(Long id);
}
