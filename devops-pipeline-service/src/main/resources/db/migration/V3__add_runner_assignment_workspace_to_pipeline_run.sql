ALTER TABLE devops_pipeline_run
    ADD COLUMN IF NOT EXISTS assigned_runner_name VARCHAR(100);

ALTER TABLE devops_pipeline_run
    ADD COLUMN IF NOT EXISTS workspace_dir VARCHAR(500);

COMMENT ON COLUMN devops_pipeline_run.assigned_runner_name IS '分配执行的Runner名称';
COMMENT ON COLUMN devops_pipeline_run.workspace_dir IS '流水线运行工作目录';

CREATE INDEX IF NOT EXISTS idx_devops_pipeline_run_assigned_runner_name
    ON devops_pipeline_run(assigned_runner_name);