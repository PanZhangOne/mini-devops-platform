package com.zpan.devops.release.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import com.zpan.devops.release.client.CodeRepositoryClient;
import com.zpan.devops.release.client.WorkProjectClient;
import com.zpan.devops.release.entity.Version;
import com.zpan.devops.release.enums.VersionStatus;
import com.zpan.devops.release.mapper.VersionMapper;
import com.zpan.devops.release.model.request.VersionCreateRequest;
import com.zpan.devops.release.model.request.VersionStatusUpdateRequest;
import com.zpan.devops.release.model.request.VersionUpdateRequest;
import com.zpan.devops.release.model.vo.VersionVO;
import com.zpan.devops.release.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final VersionMapper versionMapper;

    private final WorkProjectClient workProjectClient;

    private final CodeRepositoryClient codeRepositoryClient;

    @Override
    public VersionVO create(VersionCreateRequest request, Long currentUserId) {
        checkProjectExists(request.getProjectId());
        checkRepositoryExists(request.getRepositoryId());
        validateVersionNoNotExists(request.getProjectId(), request.getVersionNo());

        LocalDateTime now = LocalDateTime.now();

        Version version = new Version();
        version.setProjectId(request.getProjectId());
        version.setRepositoryId(request.getRepositoryId());
        version.setVersionNo(request.getVersionNo());
        version.setGitTag(request.getGitTag());
        version.setBranchName(request.getBranchName());
        version.setCommitHash(request.getCommitHash());
        version.setTitle(request.getTitle());
        version.setDescription(request.getDescription());
        version.setStatus(VersionStatus.DRAFT.name());
        version.setCreatedBy(currentUserId);
        version.setCreatedAt(now);
        version.setUpdatedAt(now);

        versionMapper.insert(version);

        return toVO(version);

    }

    @Override
    public List<VersionVO> list(Long projectId) {
        LambdaQueryWrapper<Version> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(Version::getProjectId, projectId);
        }

        wrapper.orderByDesc(Version::getCreatedAt);

        return versionMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public VersionVO getById(Long id) {
        Version version = getVersionOrThrow(id);
        return toVO(version);
    }

    @Override
    public VersionVO update(Long id, VersionUpdateRequest request) {
        Version version = getVersionOrThrow(id);

        if (!VersionStatus.isValid(request.getStatus())) {
            throw new BizException(ErrorCode.VERSION_STATUS_INVALID);
        }

        version.setStatus(request.getStatus());
        version.setUpdatedAt(LocalDateTime.now());

        if (VersionStatus.RELEASED.name().equals(request.getStatus())) {
            version.setReleasedAt(LocalDateTime.now());
        }

        versionMapper.updateById(version);

        return toVO(version);
    }

    @Override
    public VersionVO updateStatus(Long id, VersionStatusUpdateRequest request) {
        return null;
    }

    @Override
    public void delete(Long id) {
        Version version = getVersionOrThrow(id);

        if (VersionStatus.RELEASED.name().equals(version.getStatus())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "已发布版本不能删除");
        }

        versionMapper.deleteById(version.getId());
    }

    @Override
    public void checkVersionExists(Long id) {
        getVersionOrThrow(id);
    }

    private void checkProjectExists(Long projectId) {
        Result<Boolean> result = workProjectClient.existsById(projectId);
        if (!result.getData()) {
            throw new BizException(ErrorCode.PROJECT_NOT_FOUND);
        }
    }

    private void checkRepositoryExists(Long repositoryId) {
        Result<Boolean> result = codeRepositoryClient.existsById(repositoryId);
        if (!result.getData()) {
            throw new BizException(ErrorCode.REPOSITORY_NOT_FOUND);
        }
    }

    private void validateVersionNoNotExists(Long projectId, String versionNo) {
        LambdaQueryWrapper<Version> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Version::getProjectId, projectId);
        wrapper.eq(Version::getVersionNo, versionNo);

        if (versionMapper.selectCount(wrapper) > 0) {
            throw new BizException(ErrorCode.VERSION_NO_EXISTS);
        }
    }

    private Version getVersionOrThrow(Long id) {
        Version version = versionMapper.selectById(id);
        if (version == null) {
            throw new BizException(ErrorCode.VERSION_NOT_FOUND);
        }
        return version;
    }

    private VersionVO toVO(Version version) {
        VersionVO vo = new VersionVO();
        vo.setId(version.getId());
        vo.setProjectId(version.getProjectId());
        vo.setRepositoryId(version.getRepositoryId());
        vo.setVersionNo(version.getVersionNo());
        vo.setGitTag(version.getGitTag());
        vo.setBranchName(version.getBranchName());
        vo.setCommitHash(version.getCommitHash());
        vo.setTitle(version.getTitle());
        vo.setDescription(version.getDescription());
        vo.setStatus(version.getStatus());
        vo.setCreatedBy(version.getCreatedBy());
        vo.setReleasedAt(version.getReleasedAt());
        vo.setCreatedAt(version.getCreatedAt());
        vo.setUpdatedAt(version.getUpdatedAt());

        if (VersionStatus.isValid(version.getStatus())) {
            vo.setStatusDescription(VersionStatus.valueOf(version.getStatus()).getDescription());
        }

        return vo;
    }
}
