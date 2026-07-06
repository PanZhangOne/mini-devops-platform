CREATE TABLE IF NOT EXISTS devops_event_message (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    topic VARCHAR(200) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_key VARCHAR(200),
    source_service VARCHAR(100) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INT NOT NULL,
    max_retry_count INT NOT NULL,
    next_retry_at TIMESTAMP,
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_event_message IS '事件消息表';
COMMENT ON COLUMN devops_event_message.id IS '主键ID';
COMMENT ON COLUMN devops_event_message.event_id IS '事件ID，全局唯一';
COMMENT ON COLUMN devops_event_message.topic IS 'RocketMQ Topic';
COMMENT ON COLUMN devops_event_message.event_type IS '事件类型';
COMMENT ON COLUMN devops_event_message.event_key IS '事件业务Key';
COMMENT ON COLUMN devops_event_message.source_service IS '来源服务';
COMMENT ON COLUMN devops_event_message.payload_json IS '事件内容JSON';
COMMENT ON COLUMN devops_event_message.status IS '消息状态';
COMMENT ON COLUMN devops_event_message.retry_count IS '已重试次数';
COMMENT ON COLUMN devops_event_message.max_retry_count IS '最大重试次数';
COMMENT ON COLUMN devops_event_message.next_retry_at IS '下次重试时间';
COMMENT ON COLUMN devops_event_message.sent_at IS '发送成功时间';
COMMENT ON COLUMN devops_event_message.error_message IS '错误信息';
COMMENT ON COLUMN devops_event_message.created_at IS '创建时间';
COMMENT ON COLUMN devops_event_message.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_event_message_event_id
    ON devops_event_message(event_id);

CREATE INDEX IF NOT EXISTS idx_devops_event_message_status_next_retry
    ON devops_event_message(status, next_retry_at);

CREATE INDEX IF NOT EXISTS idx_devops_event_message_topic
    ON devops_event_message(topic);

CREATE INDEX IF NOT EXISTS idx_devops_event_message_event_type
    ON devops_event_message(event_type);


CREATE TABLE IF NOT EXISTS devops_event_consume_record (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    consumer_group VARCHAR(200) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    consumed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

COMMENT ON TABLE devops_event_consume_record IS '事件消费记录表';
COMMENT ON COLUMN devops_event_consume_record.id IS '主键ID';
COMMENT ON COLUMN devops_event_consume_record.event_id IS '事件ID';
COMMENT ON COLUMN devops_event_consume_record.consumer_group IS '消费者组';
COMMENT ON COLUMN devops_event_consume_record.event_type IS '事件类型';
COMMENT ON COLUMN devops_event_consume_record.status IS '消费状态';
COMMENT ON COLUMN devops_event_consume_record.error_message IS '错误信息';
COMMENT ON COLUMN devops_event_consume_record.consumed_at IS '消费成功时间';
COMMENT ON COLUMN devops_event_consume_record.created_at IS '创建时间';
COMMENT ON COLUMN devops_event_consume_record.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_devops_event_consume_event_group
    ON devops_event_consume_record(event_id, consumer_group);

CREATE INDEX IF NOT EXISTS idx_devops_event_consume_status
    ON devops_event_consume_record(status);