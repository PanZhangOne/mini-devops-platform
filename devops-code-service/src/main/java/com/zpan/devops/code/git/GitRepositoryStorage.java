package com.zpan.devops.code.git;

import com.zpan.devops.code.config.GitStorageProperties;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GitRepositoryStorage {
    private static final Pattern SAFE_SEGMENT_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final GitStorageProperties gitStorageProperties;

    public String buildRepositoryPath(String namespace, String path) {
        validatePathSegment(namespace);
        validatePathSegment(path);

        Path root = Path.of(gitStorageProperties.getRepositoryRoot()).toAbsolutePath().normalize();
        Path repositoryPath = root.resolve(namespace).resolve(path + ".git").normalize();

        if (!repositoryPath.startsWith(root)) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_PATH_INVALID);
        }
        return repositoryPath.toString();
    }

    public String buildCloneHttpUrl(String namespace, String path) {
        String baseUrl = gitStorageProperties.getCloneBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + namespace + "/" + path + "/git";
    }

    public void createBareRepository(String repositoryPath) {
        File directory = new File(repositoryPath);
        if (directory.exists()) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_EXISTS);
        }

        File parent = directory.getParentFile();
        if (parent == null && !parent.exists() && !parent.mkdirs()) {
            throw  new BizException(ErrorCode.CODE_REPOSITORY_CREATE_FAILED);
        }

        try {
            Git git = Git.init().setDirectory(directory).setBare(true).call();
            git.close();
        } catch (Exception e) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_CREATE_FAILED);
        }
    }

    public boolean exists(String repositoryPath) {
        return new File((repositoryPath)).exists();
    }

    private void validatePathSegment(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_PATH_INVALID);
        }
        if (!SAFE_SEGMENT_PATTERN.matcher(value).matches()) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_PATH_INVALID);
        }
        if (value.contains("..") || value.contains("/") || value.contains("\\")) {
            throw new BizException(ErrorCode.CODE_REPOSITORY_PATH_INVALID);
        }
    }
}
