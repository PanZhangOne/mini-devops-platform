CREATE TABLE IF NOT EXISTS devops_repository (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    repo_name VARCHAR(100) NOT NULL,
    repo_url VARCHAR(500) NOT NULL,
    default_branch VARCHAR(100) NOT NULL,
    repo_type VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_repository IS '代码仓库表';
COMMENT ON COLUMN devops_repository.id IS '仓库ID';
COMMENT ON COLUMN devops_repository.project_id IS '项目ID';
COMMENT ON COLUMN devops_repository.repo_name IS '仓库名称';
COMMENT ON COLUMN devops_repository.repo_url IS '仓库地址';
COMMENT ON COLUMN devops_repository.default_branch IS '默认分支';
COMMENT ON COLUMN devops_repository.repo_type IS '仓库类型';
COMMENT ON COLUMN devops_repository.description IS '仓库描述';
COMMENT ON COLUMN devops_repository.created_at IS '创建时间';
COMMENT ON COLUMN devops_repository.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_repository_project_id
    ON devops_repository(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_repository_repo_type
    ON devops_repository(repo_type);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_repository_project_repo_url
    ON devops_repository(project_id, repo_url);