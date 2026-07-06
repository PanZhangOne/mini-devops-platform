CREATE TABLE IF NOT EXISTS devops_task_relation (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    relation_type VARCHAR(50) NOT NULL,
    relation_id BIGINT,
    relation_key VARCHAR(200),
    relation_title VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_task_relation IS '任务关联对象表';
COMMENT ON COLUMN devops_task_relation.id IS '关联ID';
COMMENT ON COLUMN devops_task_relation.task_id IS '任务ID';
COMMENT ON COLUMN devops_task_relation.relation_type IS '关联类型';
COMMENT ON COLUMN devops_task_relation.relation_id IS '关联对象ID';
COMMENT ON COLUMN devops_task_relation.relation_key IS '关联对象Key，例如分支名、Commit Hash、MR编号';
COMMENT ON COLUMN devops_task_relation.relation_title IS '关联对象标题';
COMMENT ON COLUMN devops_task_relation.created_by IS '创建人ID';
COMMENT ON COLUMN devops_task_relation.created_at IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_devops_task_relation_task_id
    ON devops_task_relation(task_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_relation_type
    ON devops_task_relation(relation_type);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_task_relation_unique
    ON devops_task_relation(task_id, relation_type, relation_key);