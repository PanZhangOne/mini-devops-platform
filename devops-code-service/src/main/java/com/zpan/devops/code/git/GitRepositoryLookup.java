package com.zpan.devops.code.git;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.code.entity.CodeRepository;
import com.zpan.devops.code.enums.RepositoryStatus;
import com.zpan.devops.code.mapper.CodeRepositoryMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitRepositoryLookup {

    private final CodeRepositoryMapper codeRepositoryMapper;

    public CodeRepository findActiveRepository(String namespace, String path) throws RepositoryNotFoundException {
        LambdaQueryWrapper<CodeRepository> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeRepository::getNamespace, namespace);
        wrapper.eq(CodeRepository::getPath, path);
        wrapper.eq(CodeRepository::getStatus, RepositoryStatus.ACTIVE.name());

        CodeRepository repository = codeRepositoryMapper.selectOne(wrapper);
        if (repository == null) {
            throw new RepositoryNotFoundException(namespace + "/" + path + ".git");
        }
        return repository;
    }
}
