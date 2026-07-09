CREATE TABLE IF NOT EXISTS devops_code_repository (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    namespace VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    default_branch VARCHAR(100) NOT NULL,
    visibility VARCHAR(30) NOT NULL,
    repository_path VARCHAR(500) NOT NULL,
    clone_http_url VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_code_repository IS '代码仓库表';
COMMENT ON COLUMN devops_code_repository.id IS '仓库ID';
COMMENT ON COLUMN devops_code_repository.project_id IS '项目ID';
COMMENT ON COLUMN devops_code_repository.namespace IS '命名空间';
COMMENT ON COLUMN devops_code_repository.name IS '仓库名称';
COMMENT ON COLUMN devops_code_repository.path IS '仓库路径名';
COMMENT ON COLUMN devops_code_repository.description IS '仓库描述';
COMMENT ON COLUMN devops_code_repository.default_branch IS '默认分支';
COMMENT ON COLUMN devops_code_repository.visibility IS '可见性';
COMMENT ON COLUMN devops_code_repository.repository_path IS '裸仓库本地路径';
COMMENT ON COLUMN devops_code_repository.clone_http_url IS 'HTTP Clone 地址';
COMMENT ON COLUMN devops_code_repository.status IS '仓库状态';
COMMENT ON COLUMN devops_code_repository.created_by IS '创建人ID';
COMMENT ON COLUMN devops_code_repository.created_at IS '创建时间';
COMMENT ON COLUMN devops_code_repository.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_code_repository_namespace_path
    ON devops_code_repository(namespace, path);

CREATE INDEX IF NOT EXISTS idx_code_repository_project_id
    ON devops_code_repository(project_id);

CREATE INDEX IF NOT EXISTS idx_code_repository_status
    ON devops_code_repository(status);


CREATE TABLE IF NOT EXISTS devops_code_repository_member (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_code_repository_member IS '代码仓库成员表';
COMMENT ON COLUMN devops_code_repository_member.id IS '仓库成员ID';
COMMENT ON COLUMN devops_code_repository_member.repository_id IS '仓库ID';
COMMENT ON COLUMN devops_code_repository_member.user_id IS '用户ID';
COMMENT ON COLUMN devops_code_repository_member.role_code IS '角色编码';
COMMENT ON COLUMN devops_code_repository_member.created_at IS '创建时间';
COMMENT ON COLUMN devops_code_repository_member.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_code_repository_member_repo_user
    ON devops_code_repository_member(repository_id, user_id);

CREATE INDEX IF NOT EXISTS idx_code_repository_member_repository_id
    ON devops_code_repository_member(repository_id);

CREATE INDEX IF NOT EXISTS idx_code_repository_member_user_id
    ON devops_code_repository_member(user_id);

CREATE TABLE IF NOT EXISTS devops_code_branch (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    branch_name VARCHAR(200) NOT NULL,
    last_commit_hash VARCHAR(100),
    protected_branch BOOLEAN NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_code_branch IS '代码分支表';
COMMENT ON COLUMN devops_code_branch.id IS '分支ID';
COMMENT ON COLUMN devops_code_branch.repository_id IS '仓库ID';
COMMENT ON COLUMN devops_code_branch.branch_name IS '分支名称';
COMMENT ON COLUMN devops_code_branch.last_commit_hash IS '最新提交Hash';
COMMENT ON COLUMN devops_code_branch.protected_branch IS '是否保护分支';
COMMENT ON COLUMN devops_code_branch.created_by IS '创建人ID';
COMMENT ON COLUMN devops_code_branch.created_at IS '创建时间';
COMMENT ON COLUMN devops_code_branch.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_code_branch_repo_name
    ON devops_code_branch(repository_id, branch_name);

CREATE INDEX IF NOT EXISTS idx_code_branch_repository_id
    ON devops_code_branch(repository_id);

CREATE TABLE IF NOT EXISTS devops_code_commit (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    branch_name VARCHAR(200),
    commit_hash VARCHAR(100) NOT NULL,
    short_hash VARCHAR(20) NOT NULL,
    commit_message TEXT,
    author_name VARCHAR(200),
    author_email VARCHAR(200),
    committed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_code_commit IS '代码提交表';
COMMENT ON COLUMN devops_code_commit.id IS '提交ID';
COMMENT ON COLUMN devops_code_commit.repository_id IS '仓库ID';
COMMENT ON COLUMN devops_code_commit.branch_name IS '分支名称';
COMMENT ON COLUMN devops_code_commit.commit_hash IS '完整Commit Hash';
COMMENT ON COLUMN devops_code_commit.short_hash IS '短Commit Hash';
COMMENT ON COLUMN devops_code_commit.commit_message IS '提交信息';
COMMENT ON COLUMN devops_code_commit.author_name IS '作者名称';
COMMENT ON COLUMN devops_code_commit.author_email IS '作者邮箱';
COMMENT ON COLUMN devops_code_commit.committed_at IS '提交时间';
COMMENT ON COLUMN devops_code_commit.created_at IS '创建时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_code_commit_repo_hash
    ON devops_code_commit(repository_id, commit_hash);

CREATE INDEX IF NOT EXISTS idx_code_commit_repository_id
    ON devops_code_commit(repository_id);

CREATE INDEX IF NOT EXISTS idx_code_commit_branch_name
    ON devops_code_commit(branch_name);

CREATE TABLE IF NOT EXISTS devops_code_merge_request (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    source_branch VARCHAR(200) NOT NULL,
    target_branch VARCHAR(200) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,
    merge_commit_hash VARCHAR(100),
    created_by BIGINT,
    merged_by BIGINT,
    closed_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    merged_at TIMESTAMP,
    closed_at TIMESTAMP
    );

COMMENT ON TABLE devops_code_merge_request IS '合并请求表';
COMMENT ON COLUMN devops_code_merge_request.id IS 'MR ID';
COMMENT ON COLUMN devops_code_merge_request.repository_id IS '仓库ID';
COMMENT ON COLUMN devops_code_merge_request.project_id IS '项目ID';
COMMENT ON COLUMN devops_code_merge_request.source_branch IS '源分支';
COMMENT ON COLUMN devops_code_merge_request.target_branch IS '目标分支';
COMMENT ON COLUMN devops_code_merge_request.title IS '标题';
COMMENT ON COLUMN devops_code_merge_request.description IS '描述';
COMMENT ON COLUMN devops_code_merge_request.status IS '状态';
COMMENT ON COLUMN devops_code_merge_request.merge_commit_hash IS '合并提交Hash';
COMMENT ON COLUMN devops_code_merge_request.created_by IS '创建人ID';
COMMENT ON COLUMN devops_code_merge_request.merged_by IS '合并人ID';
COMMENT ON COLUMN devops_code_merge_request.closed_by IS '关闭人ID';
COMMENT ON COLUMN devops_code_merge_request.created_at IS '创建时间';
COMMENT ON COLUMN devops_code_merge_request.updated_at IS '更新时间';
COMMENT ON COLUMN devops_code_merge_request.merged_at IS '合并时间';
COMMENT ON COLUMN devops_code_merge_request.closed_at IS '关闭时间';

CREATE INDEX IF NOT EXISTS idx_code_mr_repository_id
    ON devops_code_merge_request(repository_id);

CREATE INDEX IF NOT EXISTS idx_code_mr_project_id
    ON devops_code_merge_request(project_id);

CREATE INDEX IF NOT EXISTS idx_code_mr_status
    ON devops_code_merge_request(status);

CREATE TABLE IF NOT EXISTS devops_code_push_event (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    branch_name VARCHAR(200) NOT NULL,
    before_commit_hash VARCHAR(100),
    after_commit_hash VARCHAR(100),
    pusher_id BIGINT,
    commit_count INT NOT NULL,
    event_payload_json TEXT,
    created_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_code_push_event IS '代码Push事件表';
COMMENT ON COLUMN devops_code_push_event.id IS 'Push事件ID';
COMMENT ON COLUMN devops_code_push_event.repository_id IS '仓库ID';
COMMENT ON COLUMN devops_code_push_event.branch_name IS '分支名称';
COMMENT ON COLUMN devops_code_push_event.before_commit_hash IS 'Push前Commit Hash';
COMMENT ON COLUMN devops_code_push_event.after_commit_hash IS 'Push后Commit Hash';
COMMENT ON COLUMN devops_code_push_event.pusher_id IS '推送人ID';
COMMENT ON COLUMN devops_code_push_event.commit_count IS '提交数量';
COMMENT ON COLUMN devops_code_push_event.event_payload_json IS '事件原始内容JSON';
COMMENT ON COLUMN devops_code_push_event.created_at IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_code_push_event_repository_id
    ON devops_code_push_event(repository_id);

CREATE INDEX IF NOT EXISTS idx_code_push_event_branch_name
    ON devops_code_push_event(branch_name);

CREATE INDEX IF NOT EXISTS idx_code_push_event_created_at
    ON devops_code_push_event(created_at);