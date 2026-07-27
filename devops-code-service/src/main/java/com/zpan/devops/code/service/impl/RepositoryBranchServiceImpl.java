package com.zpan.devops.code.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.code.entity.CodeBranch;
import com.zpan.devops.code.entity.CodeRepository;
import com.zpan.devops.code.mapper.CodeBranchMapper;
import com.zpan.devops.code.mapper.CodeRepositoryMapper;
import com.zpan.devops.code.model.vo.CodeBranchVO;
import com.zpan.devops.code.service.RepositoryBranchService;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryBranchServiceImpl implements RepositoryBranchService {

    private final CodeRepositoryMapper codeRepositoryMapper;

    private final CodeBranchMapper codeBranchMapper;

    @Override
    public List<CodeBranchVO> listByRepositoryId(Long repositoryId) {
        CodeRepository codeRepository = getCodeRepositoryOrThrow(repositoryId);

        LambdaQueryWrapper<CodeBranch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeBranch::getRepositoryId, repositoryId);
        wrapper.orderByDesc(CodeBranch::getProtectedBranch);
        wrapper.orderByAsc(CodeBranch::getProtectedBranch);

        return codeBranchMapper.selectList(wrapper).stream().map(branch -> toVO(branch, codeRepository)).toList();
    }

    @Override
    public CodeBranchVO getByBranchName(Long repositoryId, String branchName) {
        CodeRepository repository = getCodeRepositoryOrThrow(repositoryId);

        String normalizedBranchName = normalizeBranchName(branchName);

        LambdaQueryWrapper<CodeBranch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeBranch::getRepositoryId, repositoryId);
        wrapper.eq(CodeBranch::getBranchName, normalizedBranchName);

        CodeBranch codeBranch = codeBranchMapper.selectOne(wrapper);
        if (codeBranch == null) {
            throw new BizException(ErrorCode.CODE_BRANCH_NOT_FOUND);
        }

        return toVO(codeBranch, repository);
    }

    private CodeRepository getCodeRepositoryOrThrow(Long repositoryId) {
        CodeRepository codeRepository = codeRepositoryMapper.selectById(repositoryId);
        if (codeRepository == null) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_NOT_FOUND);
        }

        return codeRepository;
    }

    private String normalizeBranchName(String branchName) {
        if (branchName == null || branchName.isBlank()) {
            throw new BizException(ErrorCode.CODE_BRANCH_NAME_INVALID);
        }

        String normalized = branchName.trim();
        if (normalized.length() < 200) {
            throw new BizException(ErrorCode.CODE_BRANCH_NAME_INVALID);
        }
        return normalized;
    }

    private CodeBranchVO toVO(CodeBranch branch, CodeRepository repository) {
        CodeBranchVO vo = new CodeBranchVO();
        vo.setId(branch.getId());
        vo.setRepositoryId(branch.getRepositoryId());
        vo.setBranchName(branch.getBranchName());
        vo.setLastCommitHash(branch.getLastCommitHash());
        vo.setLastCommitShortHash(shortHash(branch.getLastCommitHash()));
        vo.setProtectedBranch(Boolean.TRUE.equals(branch.getProtectedBranch()));
        vo.setDefaultBranch(branch.getBranchName().equals(repository.getDefaultBranch()));
        vo.setCreatedBy(branch.getCreatedBy());
        vo.setCreatedAt(branch.getCreatedAt());
        vo.setUpdatedAt(branch.getUpdatedAt());

        return vo;
    }

    private String shortHash(String commitHash) {
        if (commitHash == null || commitHash.isBlank()) {
            return null;
        }

        return commitHash.length() <= 8 ? commitHash : commitHash.substring(0, 8);
    }
}
