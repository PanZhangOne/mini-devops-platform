package com.zpan.devops.code.service;

import com.zpan.devops.code.model.vo.CodeBranchVO;

import java.util.List;

public interface RepositoryBranchService {

    List<CodeBranchVO> listByRepositoryId(Long repositoryId);

    CodeBranchVO getByBranchName(Long repositoryId, String branchName);
}
