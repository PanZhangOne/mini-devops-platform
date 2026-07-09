package com.zpan.devops.code.git;


import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;

import java.util.Collection;

public interface GitPushService {

    void handlePush(GitPushContext context, Repository repository, Collection<ReceiveCommand> commands);
}
