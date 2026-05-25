CREATE TABLE IF NOT EXISTS devops_runner (
    id BIGSERIAL PRIMARY KEY,
    runner_name VARCHAR(100) NOT NULL UNIQUE,
    runner_token VARCHAR(200) NOT NULL,
    ip VARCHAR(100),
    port INT,
    status VARCHAR(30) NOT NULL,
    max_concurrency INT NOT NULL,
    current_concurrency INT NOT NULL,
    last_heartbeat_at TIMESTAMP,
    registered_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_runner IS 'Runner执行节点表';
COMMENT ON COLUMN devops_runner.id IS 'Runner ID';
COMMENT ON COLUMN devops_runner.runner_name IS 'Runner名称';
COMMENT ON COLUMN devops_runner.runner_token IS 'Runner认证Token';
COMMENT ON COLUMN devops_runner.ip IS 'Runner IP地址';
COMMENT ON COLUMN devops_runner.port IS 'Runner端口';
COMMENT ON COLUMN devops_runner.status IS 'Runner状态';
COMMENT ON COLUMN devops_runner.max_concurrency IS '最大并发数';
COMMENT ON COLUMN devops_runner.current_concurrency IS '当前并发数';
COMMENT ON COLUMN devops_runner.last_heartbeat_at IS '最后心跳时间';
COMMENT ON COLUMN devops_runner.registered_at IS '注册时间';
COMMENT ON COLUMN devops_runner.created_at IS '创建时间';
COMMENT ON COLUMN devops_runner.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_devops_runner_status
    ON devops_runner(status);

CREATE INDEX IF NOT EXISTS idx_devops_runner_last_heartbeat_at
    ON devops_runner(last_heartbeat_at);