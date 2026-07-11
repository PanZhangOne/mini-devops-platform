package com.zpan.devops.code.git.auth;

import lombok.Getter;

@Getter
public class GitAuthResult {

    private final boolean success;

    private final GitAuthUser user;

    private GitAuthResult(boolean success, GitAuthUser user) {
        this.success = success;
        this.user = user;
    }

    public static GitAuthResult success(GitAuthUser user) {
        return new GitAuthResult(true, user);
    }

    public static GitAuthResult failed() {
        return new GitAuthResult(false, null);
    }
}
