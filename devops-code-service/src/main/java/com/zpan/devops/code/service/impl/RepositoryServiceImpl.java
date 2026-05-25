package com.zpan.devops.code.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.code.client.WorkProjectClient;
import com.zpan.devops.code.entity.Repository;
import com.zpan.devops.code.enums.RepoType;
import com.zpan.devops.code.mapper.RepositoryMapper;
import com.zpan.devops.code.model.request.RepositoryCreateRequest;
import com.zpan.devops.code.model.request.RepositoryUpdateRequest;
import com.zpan.devops.code.model.vo.RepositoryVO;
import com.zpan.devops.code.service.RepositoryService;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryMapper repositoryMapper;

    private final WorkProjectClient workProjectClient;

    @Override
    public RepositoryVO create(RepositoryCreateRequest request) {
        checkProjectExists(request.getProjectId());
        validateRepoType(request.getRepoType());
        validateRepoUrlNotExists(request.getProjectId(), request.getRepoUrl());

        LocalDateTime now = LocalDateTime.now();
        Repository repository = new Repository();
        repository.setProjectId(request.getProjectId());
        repository.setRepoName(request.getRepoName());
        repository.setRepoUrl(request.getRepoUrl());
        repository.setDefaultBranch(request.getDefaultBranch());
        repository.setRepoType(request.getRepoType());
        repository.setDescription(request.getDescription());
        repository.setCreatedAt(now);
        repository.setUpdatedAt(now);

        repositoryMapper.insert(repository);
        return toVO(repository);
    }

    @Override
    public List<RepositoryVO> list() {
        LambdaQueryWrapper<Repository> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Repository::getCreatedAt);

        return repositoryMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<RepositoryVO> listByProjectId(Long projectId) {
        checkProjectExists(projectId);

        LambdaQueryWrapper<Repository> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Repository::getProjectId, projectId);
        wrapper.orderByDesc(Repository::getCreatedAt);

        return repositoryMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public RepositoryVO getById(Long id) {
        Repository repository = getRepositoryOrThrow(id);
        return toVO(repository);
    }

    @Override
    public RepositoryVO update(Long id, RepositoryUpdateRequest request) {
        Repository repository = getRepositoryOrThrow(id);
        validateRepoType(request.getRepoType());

        repository.setRepoName(request.getRepoName());
        repository.setDefaultBranch(request.getDefaultBranch());
        repository.setRepoType(request.getRepoType());
        repository.setDescription(request.getDescription());
        repository.setUpdatedAt(LocalDateTime.now());

        repositoryMapper.updateById(repository);

        return toVO(repository);
    }

    @Override
    public void delete(Long id) {
        Repository repository = getRepositoryOrThrow(id);
        repositoryMapper.deleteById(repository.getId());
    }

    @Override
    public boolean existsById(Long id) {
        Repository repository = repositoryMapper.selectById(id);
        return repository != null;
    }

    private void checkProjectExists(Long projectId) {
        Result<Boolean> result;
        try {
            result = workProjectClient.existsById(projectId);
        } catch (Exception e) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "调用项目服务失败");
        }

        if (result == null || result.getCode() == null) {
            throw new BizException(ErrorCode.REMOTE_SERVICE_ERROR, "项目服务返回异常");
        }
        if (result.getCode() != 0) {
            throw new BizException(result.getCode(), result.getMessage());
        }

        Boolean exists = result.getData();
        if (!Boolean.TRUE.equals(exists)) {
            throw new BizException(ErrorCode.PROJECT_NOT_FOUND);
        }
    }

    private void validateRepoType(String repoType) {
        if (!RepoType.isValid(repoType)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "仓库类型不存在");
        }
    }

    private void validateRepoUrlNotExists(Long projectId, String repoUrl) {
        LambdaQueryWrapper<Repository> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Repository::getProjectId, projectId);
        wrapper.eq(Repository::getRepoUrl, repoUrl);

        long count = repositoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ErrorCode.REPOSITORY_URL_EXISTS);
        }
    }

    private Repository getRepositoryOrThrow(Long id) {
        Repository repo = repositoryMapper.selectById(id);
        if (repo == null) {
            throw new BizException(ErrorCode.REPOSITORY_NOT_FOUND);
        }
        return repo;
    }

    private RepositoryVO toVO(Repository repository) {
        RepositoryVO vo = new RepositoryVO();
        vo.setId(repository.getId());
        vo.setProjectId(repository.getProjectId());
        vo.setRepoName(repository.getRepoName());
        vo.setRepoUrl(repository.getRepoUrl());
        vo.setDefaultBranch(repository.getDefaultBranch());
        vo.setRepoType(repository.getRepoType());
        vo.setDescription(repository.getDescription());
        vo.setCreatedAt(repository.getCreatedAt());
        vo.setUpdatedAt(repository.getUpdatedAt());

        if (RepoType.isValid(repository.getRepoType())) {
            vo.setRepoTypeDescription(RepoType.valueOf(repository.getRepoType()).getDescription());
        }

        return vo;
    }
}
