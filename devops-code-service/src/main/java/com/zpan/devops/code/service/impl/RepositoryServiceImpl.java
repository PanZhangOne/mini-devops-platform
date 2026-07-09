package com.zpan.devops.code.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpan.devops.code.client.WorkProjectClient;
import com.zpan.devops.code.entity.CodeRepository;
import com.zpan.devops.code.entity.CodeRepositoryMember;
import com.zpan.devops.code.entity.Repository;
import com.zpan.devops.code.enums.RepoType;
import com.zpan.devops.code.enums.RepositoryMemberRole;
import com.zpan.devops.code.enums.RepositoryStatus;
import com.zpan.devops.code.enums.RepositoryVisibility;
import com.zpan.devops.code.git.GitRepositoryStorage;
import com.zpan.devops.code.mapper.CodeRepositoryMapper;
import com.zpan.devops.code.mapper.CodeRepositoryMemberMapper;
import com.zpan.devops.code.mapper.RepositoryMapper;
import com.zpan.devops.code.model.request.RepositoryCreateRequest;
import com.zpan.devops.code.model.request.RepositoryListRequest;
import com.zpan.devops.code.model.request.RepositoryUpdateRequest;
import com.zpan.devops.code.model.vo.CodeRepositoryVO;
import com.zpan.devops.code.model.vo.RepositoryVO;
import com.zpan.devops.code.service.RepositoryService;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.RepositoryType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private static final String DEFAULT_BRANCH = "main";

    private final CodeRepositoryMapper codeRepositoryMapper;

    private final CodeRepositoryMemberMapper codeRepositoryMemberMapper;

    private final GitRepositoryStorage gitRepositoryStorage;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CodeRepositoryVO create(RepositoryCreateRequest request, Long currentUserId) {
        String namespace = normalize(request.getNamespace());
        String path = normalize(request.getPath());
        String visibility = normalizeVisibility(request.getVisibility());

        validateVisibility(visibility);
        validateRepositoryNotExists(namespace, path);

        String repositoryPath = gitRepositoryStorage.buildRepositoryPath(namespace, path);
        String cloneHttpUrl = gitRepositoryStorage.buildCloneHttpUrl(namespace, path);

        gitRepositoryStorage.createBareRepository(repositoryPath);
        LocalDateTime now = LocalDateTime.now();

        CodeRepository repository = new CodeRepository();
        repository.setProjectId(request.getProjectId());
        repository.setNamespace(namespace);
        repository.setName(request.getName());
        repository.setPath(path);
        repository.setDescription(request.getDescription());
        repository.setDefaultBranch(DEFAULT_BRANCH);
        repository.setVisibility(visibility);
        repository.setRepositoryPath(repositoryPath);
        repository.setCloneHttpUrl(cloneHttpUrl);
        repository.setStatus(RepositoryStatus.ACTIVE.name());
        repository.setCreatedBy(currentUserId);
        repository.setCreatedAt(now);
        repository.setUpdatedAt(now);

        codeRepositoryMapper.insert(repository);
        createOwnerMember(repository.getId(), currentUserId, now);
        return toVO(repository);
    }

    @Override
    public Page<CodeRepositoryVO> list(RepositoryListRequest request) {
        int pageNo = request.getPageNo() == null || request.getPageSize() < 1 ? 1 : request.getPageNo();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 20 : Math.min(request.getPageSize(), 100);

        LambdaQueryWrapper<CodeRepository> wrapper = new LambdaQueryWrapper<>();

        if (request.getProjectId() != null) {
            wrapper.eq(CodeRepository::getProjectId, request.getProjectId());
        }
        if (request.getNamespace() != null && !request.getNamespace().isBlank()) {
            wrapper.eq(CodeRepository::getNamespace, normalize(request.getNamespace()));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String status = request.getStatus().toUpperCase();
            if (!RepositoryStatus.isValid(status)) {
                throw new BizException(ErrorCode.CODE_REPOSITORY_STATUS_INVALID);
            }
            wrapper.eq(CodeRepository::getStatus, status);
        }
        if (request.getVisibility() != null && !request.getVisibility().isBlank()) {
            String visibility = request.getVisibility().toUpperCase();
            if (!RepositoryVisibility.isValid(visibility)) {
                throw new BizException(ErrorCode.CODE_REPOSITORY_VISIBILITY_INVALID);
            }
            wrapper.eq(CodeRepository::getVisibility, visibility);
        }
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keywords = request.getKeyword().trim();
            wrapper.and(
                    w -> w.like(CodeRepository::getName, keywords)
                            .or().like(CodeRepository::getPath, keywords)
                            .or().like(CodeRepository::getDescription, keywords)
            );
        }
        wrapper.orderByDesc(CodeRepository::getCreatedAt);
        wrapper.orderByDesc(CodeRepository::getId);

        Page<CodeRepository> page = codeRepositoryMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<CodeRepositoryVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public CodeRepositoryVO getById(Long id) {
        return toVO(getRepositoryOrThrow(id));
    }

    @Override
    public CodeRepositoryVO update(Long id, RepositoryUpdateRequest request) {
        CodeRepository repository = getRepositoryOrThrow(id);

        if (request.getVisibility() != null && !request.getVisibility().isBlank()) {
            String visibility = request.getVisibility().toUpperCase();
            if (!RepositoryVisibility.isValid(visibility)) {
                throw new BizException(ErrorCode.CODE_REPOSITORY_VISIBILITY_INVALID);
            }
            repository.setVisibility(visibility);
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            repository.setName(request.getName());
        }


        repository.setDescription(request.getDescription());
        repository.setUpdatedAt(LocalDateTime.now());

        codeRepositoryMapper.updateById(repository);
        return toVO(repository);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long id) {
        CodeRepository repository = getRepositoryOrThrow(id);

        repository.setStatus(RepositoryStatus.ARCHIVED.name());
        repository.setUpdatedAt(LocalDateTime.now());
        codeRepositoryMapper.updateById(repository);
    }

    @Override
    public void delete(Long id) {
        CodeRepository repository = getRepositoryOrThrow(id);
        codeRepositoryMapper.deleteById(repository.getId());
    }

    @Override
    public boolean existsById(Long id) {
        Long count = codeRepositoryMapper.selectCount(new LambdaQueryWrapper<CodeRepository>().eq(CodeRepository::getId, id));
        return count > 0;
    }

    private void createOwnerMember(Long repositoryId, Long userId, LocalDateTime now) {
        if (userId == null) {
            return;
        }
        CodeRepositoryMember member = new CodeRepositoryMember();
        member.setRepositoryId(repositoryId);
        member.setUserId(userId);
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        member.setRoleCode(RepositoryMemberRole.OWNER.name());
        codeRepositoryMemberMapper.insert(member);
    }

    private CodeRepository getRepositoryOrThrow(Long id) {
        CodeRepository repository = codeRepositoryMapper.selectById(id);
        if (repository == null) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_NOT_FOUND);
        }
        return repository;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return RepositoryVisibility.PUBLIC.name();
        }

        return visibility.trim().toUpperCase();
    }

    private void validateVisibility(String visibility) {
        if (!RepositoryVisibility.isValid(visibility)) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_VISIBILITY_INVALID);
        }
    }

    private void validateRepositoryNotExists(String namespace, String path) {
        LambdaQueryWrapper<CodeRepository> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeRepository::getNamespace, namespace);
        wrapper.eq(CodeRepository::getPath, path);
        Long count = codeRepositoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_EXISTS);
        }
    }

    private CodeRepositoryVO toVO(CodeRepository repository) {
        CodeRepositoryVO vo = new CodeRepositoryVO();
        vo.setId(repository.getId());
        vo.setProjectId(repository.getProjectId());
        vo.setNamespace(repository.getNamespace());
        vo.setName(repository.getName());
        vo.setPath(repository.getPath());
        vo.setDescription(repository.getDescription());
        vo.setDefaultBranch(repository.getDefaultBranch());
        vo.setVisibility(repository.getVisibility());
        vo.setRepositoryPath(repository.getRepositoryPath());
        vo.setCloneHttpUrl(repository.getCloneHttpUrl());
        vo.setStatus(repository.getStatus());
        vo.setCreatedBy(repository.getCreatedBy());
        vo.setCreatedAt(repository.getCreatedAt());
        vo.setUpdatedAt(repository.getUpdatedAt());

        if (RepositoryVisibility.isValid(repository.getVisibility())) {
            vo.setVisibilityDescription(
                    RepositoryVisibility.valueOf(repository.getVisibility()).getDescription()
            );
        }

        if (RepositoryStatus.isValid(repository.getStatus())) {
            vo.setStatusDescription(
                    RepositoryStatus.valueOf(repository.getStatus()).getDescription()
            );
        }

        return vo;
    }
}
