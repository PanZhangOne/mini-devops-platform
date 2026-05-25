CREATE TABLE IF NOT EXISTS devops_pipeline_run (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    repository_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    run_no VARCHAR(100) NOT NULL,
    env VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    image_tag VARCHAR(300),
    commit_hash VARCHAR(100),
    trigger_user_id BIGINT,
    trigger_type VARCHAR(30) NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    duration_seconds BIGINT,
    log_text TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_pipeline_run IS '流水线运行记录表';
COMMENT ON COLUMN devops_pipeline_run.id IS '流水线运行ID';
COMMENT ON COLUMN devops_pipeline_run.project_id IS '项目ID';
COMMENT ON COLUMN devops_pipeline_run.repository_id IS '代码仓库ID';
COMMENT ON COLUMN devops_pipeline_run.version_id IS '版本ID';
COMMENT ON COLUMN devops_pipeline_run.run_no IS '运行编号';
COMMENT ON COLUMN devops_pipeline_run.env IS '部署环境';
COMMENT ON COLUMN devops_pipeline_run.status IS '运行状态';
COMMENT ON COLUMN devops_pipeline_run.image_tag IS '镜像标签';
COMMENT ON COLUMN devops_pipeline_run.commit_hash IS '提交哈希';
COMMENT ON COLUMN devops_pipeline_run.trigger_user_id IS '触发用户ID';
COMMENT ON COLUMN devops_pipeline_run.trigger_type IS '触发类型';
COMMENT ON COLUMN devops_pipeline_run.started_at IS '开始时间';
COMMENT ON COLUMN devops_pipeline_run.finished_at IS '结束时间';
COMMENT ON COLUMN devops_pipeline_run.duration_seconds IS '持续秒数';
COMMENT ON COLUMN devops_pipeline_run.log_text IS '运行日志';
COMMENT ON COLUMN devops_pipeline_run.error_message IS '错误信息';
COMMENT ON COLUMN devops_pipeline_run.created_at IS '创建时间';
COMMENT ON COLUMN devops_pipeline_run.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_project_id
    ON devops_pipeline_run(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_repository_id
    ON devops_pipeline_run(repository_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_version_id
    ON devops_pipeline_run(version_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_env
    ON devops_pipeline_run(env);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_status
    ON devops_pipeline_run(status);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_created_at
    ON devops_pipeline_run(created_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_pipeline_run_run_no
    ON devops_pipeline_run(run_no);