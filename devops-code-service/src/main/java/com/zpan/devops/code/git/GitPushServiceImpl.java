package com.zpan.devops.code.git;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpan.devops.code.entity.CodeBranch;
import com.zpan.devops.code.entity.CodeCommit;
import com.zpan.devops.code.entity.CodePushEvent;
import com.zpan.devops.code.mapper.CodeBranchMapper;
import com.zpan.devops.code.mapper.CodeCommitMapper;
import com.zpan.devops.code.mapper.CodePushEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitPushServiceImpl implements GitPushService {

    private final CodeBranchMapper codeBranchMapper;

    private final CodeCommitMapper codeCommitMapper;

    private final CodePushEventMapper codePushEventMapper;

    private final ObjectMapper objectMapper;

    @Override
    public void handlePush(GitPushContext context, Repository repository, Collection<ReceiveCommand> commands) {
        for (ReceiveCommand command : commands) {
            if (command.getResult() != ReceiveCommand.Result.OK) {
                continue;
            }
            if (!isBranchRef(command.getRefName())) {
                continue;
            }
            handleBranchPush(context, repository, command);
        }
    }

    private void handleBranchPush(GitPushContext context, Repository repository, ReceiveCommand command) {
        String branchName = command.getRefName();
        String oldHash = toHash(command.getOldId());
        String newHash = toHash(command.getNewId());

        if (isDeleteCommand(command)) {
            handleBranchDelete(context, branchName, oldHash, newHash, command);
            return;
        }

        syncBranch(context, branchName, newHash);
        int commitCount = syncCommits(context, repository, branchName, command);
        savePushEvent(context, branchName, oldHash, newHash, commitCount, command);

        log.info(
                "Git push handled. repositoryId={}, branch={}, oldHash={}, newHash={}, commitCount={}",
                context.getRepositoryId(),
                branchName,
                oldHash,
                newHash,
                commitCount
        );
    }

    private void syncBranch(GitPushContext context, String branchName, String newHash) {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<CodeBranch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeBranch::getBranchName, branchName);
        wrapper.eq(CodeBranch::getRepositoryId, context.getRepositoryId());

        CodeBranch branch = codeBranchMapper.selectOne(wrapper);
        if (branch == null) {
            branch = new CodeBranch();
            branch.setBranchName(branchName);
            branch.setRepositoryId(context.getRepositoryId());
            branch.setLastCommitHash(newHash);
            branch.setProtectedBranch(false);
            branch.setCreatedAt(now);
            branch.setUpdatedAt(now);

            codeBranchMapper.insert(branch);
            return;
        }

        branch.setLastCommitHash(newHash);
        branch.setUpdatedAt(now);
        codeBranchMapper.updateById(branch);
    }

    private int syncCommits(GitPushContext context, Repository repository, String branchName, ReceiveCommand command) {
        ObjectId newId = command.getNewId();

        if (newId == null || ObjectId.zeroId().equals(newId)) {
            return 0;
        }
        int count = 0;

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit newCommit = revWalk.parseCommit(newId);

            ObjectId oldId = command.getOldId();
            if (oldId == null && ObjectId.zeroId().equals(oldId)) {
                RevCommit oldCommit = revWalk.parseCommit(oldId);
                revWalk.markUninteresting(oldCommit);
            }

            revWalk.markStart(newCommit);
            for (RevCommit commit : revWalk) {
                saveCommitIfAbsent(context, branchName, commit);
                count++;
            }

            return count;

        } catch (Exception e) {
            log.warn(
                    "Sync commits failed. repositoryId={}, branchName={}",
                    context.getRepositoryId(),
                    branchName,
                    e
            );
            return count;
        }
    }

    private void saveCommitIfAbsent(GitPushContext context, String branchName, RevCommit commit) {
        String commitHash = commit.getName();
        LambdaQueryWrapper<CodeCommit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeCommit::getRepositoryId, context.getRepositoryId());
        wrapper.eq(CodeCommit::getCommitHash, commitHash);

        Long count = codeCommitMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }

        PersonIdent author = commit.getAuthorIdent();

        CodeCommit codeCommit = new CodeCommit();
        codeCommit.setRepositoryId(context.getRepositoryId());
        codeCommit.setBranchName(branchName);
        codeCommit.setCommitHash(commitHash);
        codeCommit.setShortHash(shortHash(commitHash));
        codeCommit.setCommitMessage(commit.getFullMessage());
        codeCommit.setAuthorName(author == null ? null : author.getName());
        codeCommit.setAuthorEmail(author == null ? null : author.getEmailAddress());
        codeCommit.setCommittedAt(toLocalDateTime(author));
        codeCommit.setCreatedAt(LocalDateTime.now());

        codeCommitMapper.insert(codeCommit);
    }

    private void savePushEvent(
            GitPushContext context,
            String branchName,
            String oldHash,
            String newHash,
            int commitCount,
            ReceiveCommand command
    ) {
        CodePushEvent event = new CodePushEvent();
        event.setRepositoryId(context.getRepositoryId());
        event.setBranchName(branchName);
        event.setBeforeCommitHash(oldHash);
        event.setAfterCommitHash(newHash);
        event.setPusherId(context.getPusherId());
        event.setCommitCount(commitCount);
        event.setEventPayloadJson(toJson(command, branchName, oldHash, newHash, commitCount));
        event.setCreatedAt(LocalDateTime.now());

        codePushEventMapper.insert(event);
    }

    private void handleBranchDelete(
            GitPushContext context,
            String branchName,
            String oldHash,
            String newHash,
            ReceiveCommand command
    ) {
        savePushEvent(context, branchName, oldHash, newHash, 0, command);
        log.info(
                "Git branch deleted. repositoryId={}, branchName={}, oldHash={}",
                context.getRepositoryId(),
                branchName,
                oldHash
        );
    }

    private String toJson(
            ReceiveCommand command,
            String branchName,
            String oldHash,
            String newHash,
            int commitCount
    ) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("refName", command.getRefName());
            payload.put("type", command.getType().name());
            payload.put("branchName", branchName);
            payload.put("oldHash", oldHash);
            payload.put("newHash", newHash);
            payload.put("commitCount", commitCount);

            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }



    private boolean isBranchRef(String refName) {
        return refName != null && refName.startsWith(Constants.R_HEADS);
    }

    private String shortBranchName(String refName) {
        return Repository.shortenRefName(refName);
    }

    private boolean isDeleteCommand(ReceiveCommand command) {
        return ReceiveCommand.Type.DELETE.equals(command.getType())
                || ObjectId.zeroId().equals(command.getNewId());
    }

    private String toHash(ObjectId objectId) {
        if (objectId == null || ObjectId.zeroId().equals(objectId)) {
            return null;
        }

        return objectId.name();
    }

    private String shortHash(String commitHash) {
        if (commitHash == null) {
            return null;
        }
        return commitHash.length() <= 8 ? commitHash : commitHash.substring(0, 8);
    }

    private LocalDateTime toLocalDateTime(PersonIdent personIdent) {
        if (personIdent == null) {
            return null;
        }

        return personIdent.getWhenAsInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
