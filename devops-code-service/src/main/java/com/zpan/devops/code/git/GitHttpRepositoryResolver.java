package com.zpan.devops.code.git;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.code.entity.CodeRepository;
import com.zpan.devops.code.enums.RepositoryStatus;
import com.zpan.devops.code.mapper.CodeRepositoryMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class GitHttpRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private final GitRepositoryLookup gitRepositoryLookup;

    @Override
    public Repository open(HttpServletRequest req, String name) throws RepositoryNotFoundException {
        RepositoryName repositoryName = parseRepositoryName(name);

        CodeRepository codeRepository = gitRepositoryLookup.findActiveRepository(repositoryName.namespace, repositoryName.path);

        File gitDir = new File(codeRepository.getRepositoryPath());

        if (!gitDir.exists() || !gitDir.isDirectory()) {
            throw new RepositoryNotFoundException(name);
        }

        try {
            Repository repository =  new FileRepositoryBuilder().setGitDir(gitDir).setBare().build();
            req.setAttribute(GitRequestAttributes.REPOSITORY_ID, codeRepository.getId());
            req.setAttribute(GitRequestAttributes.REPOSITORY_NAMESPACE, codeRepository.getNamespace());
            req.setAttribute(GitRequestAttributes.REPOSITORY_PATH, codeRepository.getPath());

            return repository;
        } catch (Exception e) {
            throw new RepositoryNotFoundException(name);
        }
    }

    private RepositoryName parseRepositoryName(String name) throws RepositoryNotFoundException {
        if (name == null || name.isBlank()) {
            throw new RepositoryNotFoundException("empty repository name");
        }
        String normalized = name;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.endsWith(".git")) {
            throw new RepositoryNotFoundException(name);
        }

        normalized = normalized.substring(0, normalized.length() - ".git".length());

        String[] parts = normalized.split("/");

        if (parts.length != 2) {
            throw new RepositoryNotFoundException(name);
        }
        String namespace = parts[0];
        String path = parts[1];

        if (!isSafeSegment(namespace) || !isSafeSegment(path)) {
            throw new RepositoryNotFoundException(name);
        }
        return new RepositoryName(namespace, path);
    }

    private boolean isSafeSegment(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (value.contains("..") || value.contains("/") || value.contains("\\")) {
            return false;
        }

        return value.matches("^[a-zA-Z0-9._-]+$");
    }


    private record RepositoryName(String namespace, String path) {
    }
}
