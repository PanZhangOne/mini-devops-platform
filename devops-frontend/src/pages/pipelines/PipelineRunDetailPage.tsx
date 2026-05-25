import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Clock3, Terminal } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { pipelineApi } from '@/api/pipeline'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { FieldLabel, formatDateTime, formatDuration, pipelineStatusTone, SectionCard } from './PipelinesPage'

export function PipelineRunDetailPage() {
  const { runId } = useParams<{ runId: string }>()
  const navigate = useNavigate()
  const numericRunId = Number(runId)
  const isValidId = Number.isFinite(numericRunId) && numericRunId > 0

  const { data: run, isLoading } = useQuery({
    queryKey: ['pipeline-run', numericRunId],
    queryFn: () => pipelineApi.getPipelineRun(numericRunId),
    enabled: isValidId,
  })

  const { data: pipeline } = useQuery({
    queryKey: ['pipeline', run?.pipelineId],
    queryFn: () => pipelineApi.getPipeline(run!.pipelineId),
    enabled: Boolean(run?.pipelineId),
  })

  const { data: steps = [], isLoading: isStepsLoading } = useQuery({
    queryKey: ['pipeline-run-steps', numericRunId],
    queryFn: () => pipelineApi.listPipelineRunSteps(numericRunId),
    enabled: isValidId,
    select: (data) => (data ?? []).slice().sort((left, right) => left.sortOrder - right.sortOrder),
  })

  const { data: logs = [], isLoading: isLogsLoading } = useQuery({
    queryKey: ['pipeline-run-logs', numericRunId],
    queryFn: () => pipelineApi.listPipelineRunLogs(numericRunId),
    enabled: isValidId,
    select: (data) => (data ?? []).slice().sort((left, right) => +new Date(left.logTime) - +new Date(right.logTime)),
  })

  if (!isValidId) {
    return <div className="p-8 text-sm text-[var(--color-text-muted)]">无效的运行记录 ID。</div>
  }

  if (isLoading) {
    return (
      <div className="p-8 max-w-6xl mx-auto space-y-6">
        <div className="h-10 w-48 bg-[var(--color-surface-3)] rounded animate-pulse" />
        <div className="h-64 bg-[var(--color-surface)] border border-[var(--color-border)] rounded-[var(--radius-xl)] animate-pulse" />
      </div>
    )
  }

  if (!run) {
    return <div className="p-8 text-sm text-[var(--color-text-muted)]">未找到对应的运行记录。</div>
  }

  return (
    <div className="p-8 max-w-6xl mx-auto space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <Button
          variant="secondary"
          onClick={() => navigate(run.pipelineId ? `/pipelines/${run.pipelineId}` : '/pipelines')}
        >
          <ArrowLeft size={15} />
          返回流水线详情
        </Button>
      </div>

      <SectionCard className="p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="space-y-3 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-2xl font-semibold text-[var(--color-text)]">{run.runNo}</h1>
              <Badge variant={pipelineStatusTone[run.status]}>{run.statusDescription}</Badge>
              <Badge variant="muted">{run.triggerTypeDescription}</Badge>
            </div>
            <p className="text-sm text-[var(--color-text-muted)]">{pipeline?.name ?? `流水线 #${run.pipelineId}`}</p>
          </div>
          <div className="text-right text-sm text-[var(--color-text-subtle)]">
            <div>创建时间 {formatDateTime(run.createdAt)}</div>
            <div>结束时间 {formatDateTime(run.finishedAt)}</div>
          </div>
        </div>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4 mt-6">
          <div>
            <FieldLabel>分支</FieldLabel>
            <p className="text-sm text-[var(--color-text)] mt-1">{run.branchName || '—'}</p>
          </div>
          <div>
            <FieldLabel>环境</FieldLabel>
            <p className="text-sm text-[var(--color-text)] mt-1">{run.env || '—'}</p>
          </div>
          <div>
            <FieldLabel>镜像 Tag</FieldLabel>
            <p className="text-sm text-[var(--color-text)] mt-1 break-all">{run.imageTag || '—'}</p>
          </div>
          <div>
            <FieldLabel>耗时</FieldLabel>
            <p className="text-sm text-[var(--color-text)] mt-1">{formatDuration(run.durationSeconds)}</p>
          </div>
        </div>
      </SectionCard>

      <SectionCard className="overflow-hidden">
        <div className="p-5 border-b border-[var(--color-border)] flex items-center gap-2">
          <Clock3 size={16} className="text-[var(--color-text-muted)]" />
          <h2 className="text-lg font-semibold text-[var(--color-text)]">步骤执行</h2>
        </div>
        <div className="p-5 space-y-3">
          {isStepsLoading ? (
            <div className="text-sm text-[var(--color-text-muted)]">加载步骤中...</div>
          ) : steps.length === 0 ? (
            <div className="text-sm text-[var(--color-text-muted)]">暂无步骤运行记录。</div>
          ) : (
            steps.map((step) => (
              <div key={step.id} className="rounded-[var(--radius-lg)] border border-[var(--color-border)] p-4">
                <div className="flex items-center gap-2 flex-wrap">
                  <Badge variant="muted">#{step.sortOrder}</Badge>
                  <p className="text-sm font-medium text-[var(--color-text)]">{step.name}</p>
                  <Badge variant={pipelineStatusTone[step.status]}>{step.statusDescription}</Badge>
                </div>
                <p className="text-xs text-[var(--color-text-muted)] mt-2">{step.stepTypeDescription}</p>
                <div className="grid grid-cols-2 gap-3 mt-3 text-xs text-[var(--color-text-subtle)]">
                  <span>开始: {formatDateTime(step.startedAt)}</span>
                  <span>结束: {formatDateTime(step.finishedAt)}</span>
                  <span>耗时: {formatDuration(step.durationSeconds)}</span>
                  <span>退出码: {step.exitCode ?? '—'}</span>
                </div>
                {step.errorMessage && (
                  <p className="text-xs text-red-600 mt-3 bg-red-50 rounded-[var(--radius-md)] px-3 py-2">{step.errorMessage}</p>
                )}
              </div>
            ))
          )}
        </div>
      </SectionCard>

      <SectionCard className="overflow-hidden">
        <div className="p-5 border-b border-[var(--color-border)] flex items-center gap-2">
          <Terminal size={16} className="text-[var(--color-text-muted)]" />
          <h2 className="text-lg font-semibold text-[var(--color-text)]">运行日志</h2>
        </div>
        <div className="p-5">
          {isLogsLoading ? (
            <div className="text-sm text-[var(--color-text-muted)]">加载日志中...</div>
          ) : logs.length === 0 ? (
            <div className="text-sm text-[var(--color-text-muted)]">暂无日志。</div>
          ) : (
            <div className="max-h-[520px] overflow-auto rounded-[var(--radius-lg)] bg-slate-950 p-4 text-slate-100 text-xs font-mono space-y-2">
              {logs.map((log) => (
                <div key={log.id}>
                  <span className="text-slate-400">[{formatDateTime(log.logTime)}]</span>{' '}
                  <span className="text-sky-300">[{log.logLevel}]</span>{' '}
                  <span>{log.content}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </SectionCard>
    </div>
  )
}