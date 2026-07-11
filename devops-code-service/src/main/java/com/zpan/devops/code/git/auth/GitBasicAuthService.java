package com.zpan.devops.code.git.auth;

import com.zpan.devops.code.config.GitServletConfig;
import com.zpan.devops.code.config.GitStorageProperties;
import com.zpan.devops.code.git.GitRepositoryStorage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class GitBasicAuthService {

    private static final String BASIC_PREFIX = "Basic ";

    private final GitStorageProperties gitStorageProperties;

    public GitAuthResult authenticate(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null ||  authorization.isBlank()) {
            return  GitAuthResult.failed();
        }
        if (!authorization.startsWith(BASIC_PREFIX)) {
            return  GitAuthResult.failed();
        }

        String encoded  =  authorization.substring(BASIC_PREFIX.length()).trim();
        String decoded;

        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return  GitAuthResult.failed();
        }

        int separatorIndex = decoded.indexOf(':');
        if (separatorIndex <= 0) {
            return   GitAuthResult.failed();
        }

        String username = decoded.substring(0, separatorIndex);
        String token = decoded.substring(separatorIndex + 1);

        String expectedUsername = gitStorageProperties.getBasicAuth().getUsername();
        String expectedToken =  gitStorageProperties.getBasicAuth().getToken();
        Long userId = gitStorageProperties.getBasicAuth().getUserId();

        if (safeEquals(expectedUsername, username)) {
            return GitAuthResult.failed();
        }
        if (safeEquals(expectedToken, token)) {
            return GitAuthResult.failed();
        }

        return GitAuthResult.success(new GitAuthUser(userId, username));
    }

    private boolean safeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return  false;
        }
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);

        if (expectedBytes.length != actualBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expectedBytes.length; i++) {
            result |= expectedBytes[i] ^ actualBytes[i];
        }
        return result == 0;
    }
}
