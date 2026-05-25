CREATE TABLE IF NOT EXISTS devops_pipeline
(
    id            BIGSERIAL PRIMARY KEY,
    project_id    BIGINT       NOT NULL,
    repository_id BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    code          VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    trigger_type  VARCHAR(30)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by    BIGINT,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

COMMENT ON TABLE devops_pipeline IS '流水线定义表';
COMMENT ON COLUMN devops_pipeline.id IS '流水线ID';
COMMENT ON COLUMN devops_pipeline.project_id IS '项目ID';
COMMENT ON COLUMN devops_pipeline.repository_id IS '代码仓库ID';
COMMENT ON COLUMN devops_pipeline.name IS '流水线名称';
COMMENT ON COLUMN devops_pipeline.code IS '流水线编码';
COMMENT ON COLUMN devops_pipeline.description IS '流水线描述';
COMMENT ON COLUMN devops_pipeline.trigger_type IS '触发类型';
COMMENT ON COLUMN devops_pipeline.enabled IS '是否启用';
COMMENT ON COLUMN devops_pipeline.created_by IS '创建人ID';
COMMENT ON COLUMN devops_pipeline.created_at IS '创建时间';
COMMENT ON COLUMN devops_pipeline.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_pipeline_project_code
    ON devops_pipeline(project_id, code);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_project_id
    ON devops_pipeline(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_repository_id
    ON devops_pipeline(repository_id);

CREATE TABLE IF NOT EXISTS devops_pipeline_step
(
    id          BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    step_type   VARCHAR(30)  NOT NULL,
    sort_order  INT          NOT NULL,
    command     TEXT,
    config_json TEXT,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

COMMENT ON TABLE devops_pipeline_step IS '流水线步骤定义表';
COMMENT ON COLUMN devops_pipeline_step.id IS '步骤ID';
COMMENT ON COLUMN devops_pipeline_step.pipeline_id IS '流水线ID';
COMMENT ON COLUMN devops_pipeline_step.name IS '步骤名称';
COMMENT ON COLUMN devops_pipeline_step.step_type IS '步骤类型';
COMMENT ON COLUMN devops_pipeline_step.sort_order IS '排序';
COMMENT ON COLUMN devops_pipeline_step.command IS '执行命令';
COMMENT ON COLUMN devops_pipeline_step.config_json IS '步骤扩展配置JSON';
COMMENT ON COLUMN devops_pipeline_step.enabled IS '是否启用';
COMMENT ON COLUMN devops_pipeline_step.created_at IS '创建时间';
COMMENT ON COLUMN devops_pipeline_step.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_step_pipeline_id
    ON devops_pipeline_step(pipeline_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_pipeline_step_pipeline_order
    ON devops_pipeline_step(pipeline_id, sort_order);

CREATE TABLE IF NOT EXISTS devops_pipeline_run
(
    id               BIGSERIAL PRIMARY KEY,
    pipeline_id      BIGINT       NOT NULL,
    pipeline_name    VARCHAR(100),
    project_id       BIGINT       NOT NULL,
    repository_id    BIGINT       NOT NULL,
    version_id       BIGINT,
    run_no           VARCHAR(100) NOT NULL,
    branch_name      VARCHAR(100),
    commit_hash      VARCHAR(100),
    image_tag        VARCHAR(300),
    env              VARCHAR(30),
    status           VARCHAR(30)  NOT NULL,
    trigger_user_id  BIGINT,
    trigger_type     VARCHAR(30)  NOT NULL,
    trigger_by_name  VARCHAR(50),
    started_at       TIMESTAMP,
    finished_at      TIMESTAMP,
    duration_seconds BIGINT,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

COMMENT ON TABLE devops_pipeline_run IS '流水线运行记录表';
COMMENT ON COLUMN devops_pipeline_run.id IS '运行记录ID';
COMMENT ON COLUMN devops_pipeline_run.pipeline_id IS '流水线ID';
COMMENT ON COLUMN devops_pipeline_run.pipeline_name IS '流水线名称';
COMMENT ON COLUMN devops_pipeline_run.project_id IS '项目ID';
COMMENT ON COLUMN devops_pipeline_run.repository_id IS '代码仓库ID';
COMMENT ON COLUMN devops_pipeline_run.version_id IS '版本ID';
COMMENT ON COLUMN devops_pipeline_run.run_no IS '运行编号';
COMMENT ON COLUMN devops_pipeline_run.branch_name IS '分支名称';
COMMENT ON COLUMN devops_pipeline_run.commit_hash IS '提交哈希';
COMMENT ON COLUMN devops_pipeline_run.image_tag IS '镜像标签';
COMMENT ON COLUMN devops_pipeline_run.env IS '环境';
COMMENT ON COLUMN devops_pipeline_run.status IS '状态';
COMMENT ON COLUMN devops_pipeline_run.trigger_user_id IS '触发用户ID';
COMMENT ON COLUMN devops_pipeline_run.trigger_type IS '触发类型';
COMMENT ON COLUMN devops_pipeline_run.trigger_by_name IS '触发人姓名';
COMMENT ON COLUMN devops_pipeline_run.started_at IS '开始时间';
COMMENT ON COLUMN devops_pipeline_run.finished_at IS '结束时间';
COMMENT ON COLUMN devops_pipeline_run.duration_seconds IS '持续时间(秒)';
COMMENT ON COLUMN devops_pipeline_run.created_at IS '创建时间';
COMMENT ON COLUMN devops_pipeline_run.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_pipeline_run_run_no
    ON devops_pipeline_run(run_no);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_pipeline_id
    ON devops_pipeline_run(pipeline_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_project_id
    ON devops_pipeline_run(project_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_status
    ON devops_pipeline_run(status);

CREATE TABLE IF NOT EXISTS devops_pipeline_step_run
(
    id               BIGSERIAL PRIMARY KEY,
    pipeline_run_id  BIGINT       NOT NULL,
    pipeline_step_id BIGINT,
    name             VARCHAR(100) NOT NULL,
    step_type        VARCHAR(30)  NOT NULL,
    sort_order       INT          NOT NULL,
    command          TEXT,
    config_json      TEXT,
    status           VARCHAR(30)  NOT NULL,
    started_at       TIMESTAMP,
    finished_at      TIMESTAMP,
    duration_seconds BIGINT,
    exit_code        INT,
    error_message    TEXT,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

COMMENT ON TABLE devops_pipeline_step_run IS '流水线步骤运行记录表';
COMMENT ON COLUMN devops_pipeline_step_run.id IS '步骤运行ID';
COMMENT ON COLUMN devops_pipeline_step_run.pipeline_run_id IS '流水线运行ID';
COMMENT ON COLUMN devops_pipeline_step_run.pipeline_step_id IS '流水线步骤ID';
COMMENT ON COLUMN devops_pipeline_step_run.name IS '步骤名称';
COMMENT ON COLUMN devops_pipeline_step_run.step_type IS '步骤类型';
COMMENT ON COLUMN devops_pipeline_step_run.sort_order IS '排序';
COMMENT ON COLUMN devops_pipeline_step_run.command IS '执行命令';
COMMENT ON COLUMN devops_pipeline_step_run.config_json IS '步骤扩展配置JSON';
COMMENT ON COLUMN devops_pipeline_step_run.status IS '状态';
COMMENT ON COLUMN devops_pipeline_step_run.started_at IS '开始时间';
COMMENT ON COLUMN devops_pipeline_step_run.finished_at IS '结束时间';
COMMENT ON COLUMN devops_pipeline_step_run.duration_seconds IS '持续时间(秒)';
COMMENT ON COLUMN devops_pipeline_step_run.exit_code IS '退出码';
COMMENT ON COLUMN devops_pipeline_step_run.error_message IS '错误信息';
COMMENT ON COLUMN devops_pipeline_step_run.created_at IS '创建时间';
COMMENT ON COLUMN devops_pipeline_step_run.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_step_run_pipeline_run_id
    ON devops_pipeline_step_run(pipeline_run_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_step_run_status
    ON devops_pipeline_step_run(status);

CREATE TABLE IF NOT EXISTS devops_pipeline_log
(
    id              BIGSERIAL PRIMARY KEY,
    pipeline_run_id BIGINT      NOT NULL,
    step_run_id     BIGINT,
    log_time        TIMESTAMP   NOT NULL,
    log_level       VARCHAR(20) NOT NULL,
    content         TEXT        NOT NULL,
    created_at      TIMESTAMP   NOT NULL
);

COMMENT ON TABLE devops_pipeline_log IS '流水线日志表';
COMMENT ON COLUMN devops_pipeline_log.id IS '日志ID';
COMMENT ON COLUMN devops_pipeline_log.pipeline_run_id IS '流水线运行ID';
COMMENT ON COLUMN devops_pipeline_log.step_run_id IS '步骤运行ID';
COMMENT ON COLUMN devops_pipeline_log.log_time IS '日志时间';
COMMENT ON COLUMN devops_pipeline_log.log_level IS '日志级别';
COMMENT ON COLUMN devops_pipeline_log.content IS '日志内容';
COMMENT ON COLUMN devops_pipeline_log.created_at IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_log_pipeline_run_id
    ON devops_pipeline_log(pipeline_run_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_log_step_run_id
    ON devops_pipeline_log(step_run_id);

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_log_log_time
    ON devops_pipeline_log(log_time);