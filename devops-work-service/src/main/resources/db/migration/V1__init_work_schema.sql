CREATE TABLE IF NOT EXISTS devops_project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    owner_id BIGINT,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_project IS '项目表';
COMMENT ON COLUMN devops_project.id IS '项目ID';
COMMENT ON COLUMN devops_project.name IS '项目名称';
COMMENT ON COLUMN devops_project.code IS '项目编码';
COMMENT ON COLUMN devops_project.description IS '项目描述';
COMMENT ON COLUMN devops_project.owner_id IS '项目负责人ID';
COMMENT ON COLUMN devops_project.status IS '项目状态';
COMMENT ON COLUMN devops_project.created_at IS '创建时间';
COMMENT ON COLUMN devops_project.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_project_owner_id
    ON devops_project(owner_id);

CREATE INDEX IF NOT EXISTS idx_devops_project_status
    ON devops_project(status);

CREATE INDEX IF NOT EXISTS idx_devops_project_created_at
    ON devops_project(created_at);