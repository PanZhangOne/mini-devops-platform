CREATE TABLE IF NOT EXISTS devops_credential (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    name VARCHAR(100) NOT NULL,
    credential_type VARCHAR(50) NOT NULL,
    username VARCHAR(200),
    secret_value TEXT NOT NULL,
    description VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_credential IS '凭据表';
COMMENT ON COLUMN devops_credential.id IS '凭据ID';
COMMENT ON COLUMN devops_credential.project_id IS '项目ID，可为空，空表示全局凭据';
COMMENT ON COLUMN devops_credential.name IS '凭据名称';
COMMENT ON COLUMN devops_credential.credential_type IS '凭据类型';
COMMENT ON COLUMN devops_credential.username IS '用户名';
COMMENT ON COLUMN devops_credential.secret_value IS '敏感值，当前课程先明文保存，后续升级加密';
COMMENT ON COLUMN devops_credential.description IS '凭据描述';
COMMENT ON COLUMN devops_credential.created_by IS '创建人ID';
COMMENT ON COLUMN devops_credential.created_at IS '创建时间';
COMMENT ON COLUMN devops_credential.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_credential_project_id
    ON devops_credential(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_credential_type
    ON devops_credential(credential_type);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_credential_project_name
    ON devops_credential(project_id, name);