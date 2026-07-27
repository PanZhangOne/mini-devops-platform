package com.zpan.devops.code.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpan.devops.code.model.request.CommitListRequest;
import com.zpan.devops.code.model.vo.CodeCommitVO;

public interface RepositoryCommitService {

    Page<CodeCommitVO> list(Long repositoryId, CommitListRequest request);


    CodeCommitVO getByCommitHash(Long repositoryId, String commitHash);
}
