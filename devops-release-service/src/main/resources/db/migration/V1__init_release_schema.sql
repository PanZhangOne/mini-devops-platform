CREATE TABLE IF NOT EXISTS devops_version (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    repository_id BIGINT NOT NULL,
    version_no VARCHAR(50) NOT NULL,
    git_tag VARCHAR(100),
    branch_name VARCHAR(100),
    commit_hash VARCHAR(100),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,
    created_by BIGINT,
    released_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_version IS '版本表';
COMMENT ON COLUMN devops_version.id IS '版本ID';
COMMENT ON COLUMN devops_version.project_id IS '项目ID';
COMMENT ON COLUMN devops_version.repository_id IS '代码仓库ID';
COMMENT ON COLUMN devops_version.version_no IS '版本号';
COMMENT ON COLUMN devops_version.git_tag IS 'Git Tag';
COMMENT ON COLUMN devops_version.branch_name IS '分支名称';
COMMENT ON COLUMN devops_version.commit_hash IS '提交哈希';
COMMENT ON COLUMN devops_version.title IS '版本标题';
COMMENT ON COLUMN devops_version.description IS '版本描述';
COMMENT ON COLUMN devops_version.status IS '版本状态';
COMMENT ON COLUMN devops_version.created_by IS '创建人ID';
COMMENT ON COLUMN devops_version.released_at IS '发布时间';
COMMENT ON COLUMN devops_version.created_at IS '创建时间';
COMMENT ON COLUMN devops_version.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_version_project_id
    ON devops_version(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_version_repository_id
    ON devops_version(repository_id);

CREATE INDEX IF NOT EXISTS idx_devops_version_status
    ON devops_version(status);

CREATE INDEX IF NOT EXISTS idx_devops_version_created_at
    ON devops_version(created_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_version_project_version_no
    ON devops_version(project_id, version_no);