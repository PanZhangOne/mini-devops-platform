package com.zpan.devops.code.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpan.devops.code.entity.CodeCommit;
import com.zpan.devops.code.entity.CodeRepository;
import com.zpan.devops.code.mapper.CodeCommitMapper;
import com.zpan.devops.code.mapper.CodeRepositoryMapper;
import com.zpan.devops.code.model.request.CommitListRequest;
import com.zpan.devops.code.model.vo.CodeCommitVO;
import com.zpan.devops.code.service.RepositoryCommitService;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepositoryCommitServiceImpl implements RepositoryCommitService {

    private final CodeRepositoryMapper codeRepositoryMapper;

    private final CodeCommitMapper codeCommitMapper;

    @Override
    public Page<CodeCommitVO> list(Long repositoryId, CommitListRequest request) {
        CodeRepository repository = getCodeRepositoryOrThrow(repositoryId);

        LambdaQueryWrapper<CodeCommit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeCommit::getRepositoryId, repository.getId());
        if (request.getBranchName() != null && !request.getBranchName().isBlank()) {
            wrapper.eq(CodeCommit::getBranchName, request.getBranchName());
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword();

            wrapper.and(item ->
                    item.like(CodeCommit::getBranchName, keyword)
                            .or()
                            .like(CodeCommit::getCommitHash, keyword)
                            .or()
                            .like(CodeCommit::getAuthorName, keyword)
                            .or()
                            .like(CodeCommit::getAuthorEmail, keyword)
            );
        }
        wrapper.orderByDesc(CodeCommit::getCommittedAt);
        wrapper.orderByDesc(CodeCommit::getId);

        Page<CodeCommit> commitPage = codeCommitMapper.selectPage(new Page<>(request.getPageNo(), request.getPageSize()), wrapper);

        Page<CodeCommitVO> voPage = new Page<>(
                commitPage.getCurrent(),
                commitPage.getSize(),
                commitPage.getTotal()
        );
        voPage.setPages(commitPage.getPages());
        voPage.setRecords(commitPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public CodeCommitVO getByCommitHash(Long repositoryId, String commitHash) {
        getCodeRepositoryOrThrow(repositoryId);

        String normalizedHash = normalizeCommitHash(commitHash);

        LambdaQueryWrapper<CodeCommit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeCommit::getRepositoryId, repositoryId);

        if (normalizedHash.length() == 40) {
            wrapper.eq(CodeCommit::getCommitHash, normalizedHash);
        } else {
            wrapper.likeRight(CodeCommit::getCommitHash, normalizedHash);
        }
        wrapper.orderByDesc(CodeCommit::getId);
        wrapper.last("LIMIT 1");

        var commits = codeCommitMapper.selectList(wrapper);
        if (commits.isEmpty()) {
            throw new BizException(ErrorCode.CODE_COMMIT_NOT_FOUND);
        }

        if (commits.size() > 1) {
            throw new BizException(ErrorCode.CODE_COMMIT_HASH_INVALID);
        }

        return toVO(commits.getFirst());
    }

    private CodeRepository getCodeRepositoryOrThrow(Long repositoryId) {
        CodeRepository repository = codeRepositoryMapper.selectById(repositoryId);
        if (repository == null) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_NOT_FOUND);
        }
        return repository;
    }

    private CodeCommitVO toVO(CodeCommit commit) {
        CodeCommitVO vo = new CodeCommitVO();
        vo.setId(commit.getId());
        vo.setRepositoryId(commit.getRepositoryId());
        vo.setBranchName(commit.getBranchName());
        vo.setCommitHash(commit.getCommitHash());
        vo.setShortHash(commit.getShortHash());
        vo.setCommitMessage(commit.getCommitMessage());
        vo.setCommitTitle(extractCommitTitle(commit.getCommitMessage()));
        vo.setAuthorName(commit.getAuthorName());
        vo.setAuthorEmail(commit.getAuthorEmail());
        vo.setCommittedAt(commit.getCommittedAt());
        vo.setCreatedAt(commit.getCreatedAt());

        return vo;
    }

    private String extractCommitTitle(String commitMessage) {
        if (StringUtils.isEmpty(commitMessage)) {
            return null;
        }

        int lineBreakIndex = commitMessage.indexOf('\n');
        if (lineBreakIndex < 0) {
            return commitMessage;
        }

        return commitMessage.substring(0, lineBreakIndex).trim();
    }

    private String normalizeCommitHash(String commitHash) {
        if (commitHash == null || commitHash.isBlank()) {
            throw new BizException(ErrorCode.CODE_COMMIT_HASH_INVALID);
        }

        String normalized = commitHash.trim().toLowerCase();

        if (normalized.length() < 7 || normalized.length() > 40) {
            throw new BizException(ErrorCode.CODE_COMMIT_HASH_INVALID);
        }

        if (!normalized.matches("^[0-9a-f]+$")) {
            throw new BizException(ErrorCode.CODE_COMMIT_HASH_INVALID);
        }
        return normalized;
    }
}
