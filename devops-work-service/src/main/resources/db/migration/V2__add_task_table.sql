
CREATE TABLE IF NOT EXISTS devops_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assignee_id BIGINT,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    deadline TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);


COMMENT ON TABLE devops_task IS '任务表';
COMMENT ON COLUMN devops_task.id IS '任务ID';
COMMENT ON COLUMN devops_task.project_id IS '项目ID';
COMMENT ON COLUMN devops_task.title IS '任务标题';
COMMENT ON COLUMN devops_task.description IS '任务描述';
COMMENT ON COLUMN devops_task.assignee_id IS '任务负责人ID';
COMMENT ON COLUMN devops_task.status IS '任务状态';
COMMENT ON COLUMN devops_task.priority IS '任务优先级';
COMMENT ON COLUMN devops_task.deadline IS '截止时间';
COMMENT ON COLUMN devops_task.created_at IS '创建时间';
COMMENT ON COLUMN devops_task.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_task_project_id
    ON devops_task(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_assignee_id
    ON devops_task(assignee_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_status
    ON devops_task(status);

CREATE INDEX IF NOT EXISTS idx_devops_task_priority
    ON devops_task(priority);

CREATE INDEX IF NOT EXISTS idx_devops_task_created_at
    ON devops_task(created_at);