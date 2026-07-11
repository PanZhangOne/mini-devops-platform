package com.zpan.devops.code.git;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitReceivePackFactory implements ReceivePackFactory<HttpServletRequest> {

    private final GitPushService gitPushService;

    @Override
    public ReceivePack create(HttpServletRequest request, Repository repository) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        GitPushContext context = buildContext(request);

        ReceivePack receivePack = new ReceivePack(repository);
        receivePack.setPostReceiveHook(buildPostReceiveHook(context));

        return receivePack;
    }

    private PostReceiveHook buildPostReceiveHook(GitPushContext context) {
        return (receivePack, commands) -> gitPushService.handlePush(context, receivePack.getRepository(), commands);
    }

    private GitPushContext buildContext(HttpServletRequest request) {
        GitPushContext context = new GitPushContext();

        Object repositoryId = request.getAttribute(GitRequestAttributes.REPOSITORY_ID);
        Object namespace = request.getAttribute(GitRequestAttributes.REPOSITORY_NAMESPACE);
        Object repositoryPath = request.getAttribute(GitRequestAttributes.REPOSITORY_PATH);
        Object gitUserId = request.getAttribute(GitRequestAttributes.GIT_USER_ID);

        if (repositoryId instanceof Long value) {
            context.setRepositoryId(value);
        }

        if (namespace instanceof String value) {
            context.setNamespace(value);
        }
        if (repositoryPath instanceof String value) {
            context.setRepositoryPath(value);
        }
        if (gitUserId instanceof  Long value) {
            context.setPusherId(value);
        }
        if (context.getRepositoryId() == null) {
            throw new IllegalStateException("Git repository id is missing in request attributes");
        }


        return context;
    }
}
