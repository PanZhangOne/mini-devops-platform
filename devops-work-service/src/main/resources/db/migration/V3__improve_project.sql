CREATE TABLE IF NOT EXISTS devops_project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_project_member_project_user
    ON devops_project_member(project_id, user_id);

CREATE INDEX IF NOT EXISTS idx_devops_project_member_project_id
    ON devops_project_member(project_id);


CREATE TABLE IF NOT EXISTS devops_project_module (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    parent_id BIGINT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort_order INT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_project_module_project_code
    ON devops_project_module(project_id, code);

CREATE INDEX IF NOT EXISTS idx_devops_project_module_project_id
    ON devops_project_module(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_project_module_parent_id
    ON devops_project_module(parent_id);


CREATE TABLE IF NOT EXISTS devops_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    module_id BIGINT,
    parent_task_id BIGINT,
    task_no VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    task_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    assignee_id BIGINT,
    reporter_id BIGINT,
    created_by BIGINT,
    due_date TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    estimated_hours NUMERIC(10, 2),
    actual_hours NUMERIC(10, 2),
    sort_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_task_project_task_no
    ON devops_task(project_id, task_no);

CREATE INDEX IF NOT EXISTS idx_devops_task_project_id
    ON devops_task(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_module_id
    ON devops_task(module_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_parent_task_id
    ON devops_task(parent_task_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_status
    ON devops_task(status);

CREATE INDEX IF NOT EXISTS idx_devops_task_assignee_id
    ON devops_task(assignee_id);


CREATE TABLE IF NOT EXISTS devops_task_property (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    property_type VARCHAR(30) NOT NULL,
    required BOOLEAN NOT NULL,
    options_json TEXT,
    sort_order INT NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_task_property_project_code
    ON devops_task_property(project_id, code);

CREATE INDEX IF NOT EXISTS idx_devops_task_property_project_id
    ON devops_task_property(project_id);


CREATE TABLE IF NOT EXISTS devops_task_property_value (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    property_code VARCHAR(100) NOT NULL,
    value_text TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_task_property_value_task_property
    ON devops_task_property_value(task_id, property_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_property_value_task_id
    ON devops_task_property_value(task_id);


CREATE TABLE IF NOT EXISTS devops_task_comment (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    parent_id BIGINT,
    content TEXT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_devops_task_comment_task_id
    ON devops_task_comment(task_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_comment_parent_id
    ON devops_task_comment(parent_id);


CREATE TABLE IF NOT EXISTS devops_task_activity (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_content TEXT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_devops_task_activity_task_id
    ON devops_task_activity(task_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_activity_created_at
    ON devops_task_activity(created_at);


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

CREATE INDEX IF NOT EXISTS idx_devops_task_relation_task_id
    ON devops_task_relation(task_id);

CREATE INDEX IF NOT EXISTS idx_devops_task_relation_type
    ON devops_task_relation(relation_type);