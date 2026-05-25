import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, ExternalLink, Play, Plus, Settings2, Trash2 } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { pipelineApi } from '@/api/pipeline'
import { reposApi } from '@/api/code'
import { releasesApi } from '@/api/release'
import { projectsApi } from '@/api/work'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import {
  FieldLabel,
  formatDateTime,
  PipelineFormDialog,
  pipelineStatusTone,
  RunPipelineDialog,
  SectionCard,
  StepFormDialog,
} from './PipelinesPage'
import type { PipelineStepDefinition } from '@/types'

export function PipelineDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const pipelineId = Number(id)
  const isValidId = Number.isFinite(pipelineId) && pipelineId > 0

  const [pipelineDialogOpen, setPipelineDialogOpen] = useState(false)
  const [stepDialogOpen, setStepDialogOpen] = useState(false)
  const [editingStep, setEditingStep] = useState<PipelineStepDefinition | null>(null)
  const [runDialogOpen, setRunDialogOpen] = useState(false)

  const { data: projects = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (data) => data ?? [],
  })

  const { data: repositories = [] } = useQuery({
    queryKey: ['repositories'],
    queryFn: () => reposApi.list(),
    select: (data) => data ?? [],
  })

  const { data: pipeline, isLoading } = useQuery({
    queryKey: ['pipeline', pipelineId],
    queryFn: () => pipelineApi.getPipeline(pipelineId),
    enabled: isValidId,
  })

  const { data: steps = [], isLoading: isStepsLoading } = useQuery({
    queryKey: ['pipeline-steps', pipelineId],
    queryFn: () => pipelineApi.listPipelineSteps(pipelineId),
    enabled: isValidId,
    select: (data) => (data ?? []).slice().sort((left, right) => left.sortOrder - right.sortOrder),
  })

  const { data: runs = [], isLoading: isRunsLoading } = useQuery({
    queryKey: ['pipeline-runs', { pipelineId }],
    queryFn: () => pipelineApi.listPipelineRuns({ pipelineId }),
    enabled: isValidId,
    select: (data) => (data ?? []).slice().sort((left, right) => +new Date(right.createdAt) - +new Date(left.createdAt)),
  })

  const { data: versions = [] } = useQuery({
    queryKey: ['versions-for-pipeline', pipeline?.projectId],
    queryFn: () => releasesApi.list(pipeline?.projectId),
    enabled: Boolean(pipeline?.projectId),
    select: (data) => data ?? [],
  })

  const removePipelineMutation = useMutation({
    mutationFn: () => pipelineApi.removePipeline(pipelineId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['pipelines'] })
      qc.invalidateQueries({ queryKey: ['pipeline-runs'] })
      navigate('/pipelines')
    },
  })

  const removeStepMutation = useMutation({
    mutationFn: (stepId: number) => pipelineApi.removePipelineStep(stepId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['pipeline-steps', pipelineId] })
    },
  })

  const projectName = projects.find((project) => project.id === pipeline?.projectId)?.name
  const repoName = repositories.find((repo) => repo.id === pipeline?.repositoryId)?.repoName

  if (!isValidId) {
    return <div className="p-8 text-sm text-[var(--color-text-muted)]">无效的流水线 ID。</div>
  }

  if (isLoading) {
    return (
      <div className="p-8 max-w-7xl mx-auto space-y-6">
        <div className="h-10 w-48 bg-[var(--color-surface-3)] rounded animate-pulse" />
        <div className="h-56 bg-[var(--color-surface)] border border-[var(--color-border)] rounded-[var(--radius-xl)] animate-pulse" />
      </div>
    )
  }

  if (!pipeline) {
    return <div className="p-8 text-sm text-[var(--color-text-muted)]">未找到对应的流水线。</div>
  }

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <Button variant="secondary" onClick={() => navigate('/pipelines')}>
          <ArrowLeft size={15} />
          返回流水线中心
        </Button>
        <div className="flex items-center gap-2 flex-wrap">
          <Button variant="secondary" onClick={() => setPipelineDialogOpen(true)}>
            <Settings2 size={15} />
            编辑流水线
          </Button>
          <Button variant="secondary" onClick={() => setRunDialogOpen(true)}>
            <Play size={15} />
            手动运行
          </Button>
          <Button
            variant="danger"
            onClick={() => {
              if (confirm(`删除流水线“${pipeline.name}”？`)) {
                removePipelineMutation.mutate()
              }
            }}
          >
            <Trash2 size={15} />
            删除
          </Button>
        </div>
      </div>

      <SectionCard className="p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="space-y-3 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-2xl font-semibold text-[var(--color-text)]">{pipeline.name}</h1>
              <Badge variant={pipeline.enabled ? 'success' : 'warning'}>{pipeline.enabled ? '启用中' : '已停用'}</Badge>
              <Badge variant="muted">{pipeline.triggerTypeDescription}</Badge>
            </div>
            <p className="text-sm text-[var(--color-text-muted)]">{pipeline.description || '暂无描述'}</p>
          </div>
          <div className="text-right text-sm text-[var(--color-text-subtle)]">
            <div>更新时间 {formatDateTime(pipeline.updatedAt)}</div>
            <div>创建时间 {formatDateTime(pipeline.createdAt)}</div>
          </div>
        </div>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4 mt-6">
          <div>
            <FieldLabel>编码</FieldLabel>
            <p className="text-sm font-mono text-[var(--color-text)] mt-1">{pipeline.code}</p>
          </div>
          <div>
            <FieldLabel>项目</FieldLabel>
            <p className="text-sm text-[var(--color-text)] mt-1">{projectName ?? `项目 #${pipeline.projectId}`}</p>
          </div>
          <div>
            <FieldLabel>仓库</FieldLabel>
            <p className="text-sm text-[var(--color-text)] mt-1">{repoName ?? `仓库 #${pipeline.repositoryId}`}</p>
          </div>
          <div>
            <FieldLabel>最近运行</FieldLabel>
            <p className="text-sm text-[var(--color-text)] mt-1">{runs[0] ? formatDateTime(runs[0].createdAt) : '暂无运行'}</p>
          </div>
        </div>
      </SectionCard>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
        <SectionCard className="overflow-hidden">
          <div className="p-5 border-b border-[var(--color-border)] flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-[var(--color-text)]">步骤编排</h2>
              <p className="text-sm text-[var(--color-text-muted)] mt-1">直接管理定义好的执行步骤。</p>
            </div>
            <Button
              size="sm"
              onClick={() => {
                setEditingStep(null)
                setStepDialogOpen(true)
              }}
            >
              <Plus size={14} />
              新增步骤
            </Button>
          </div>
          <div className="divide-y divide-[var(--color-border)]">
            {isStepsLoading ? (
              Array.from({ length: 3 }).map((_, index) => (
                <div key={index} className="p-5 animate-pulse space-y-3">
                  <div className="h-4 bg-[var(--color-surface-3)] rounded w-1/3" />
                  <div className="h-3 bg-[var(--color-surface-3)] rounded w-2/3" />
                </div>
              ))
            ) : steps.length === 0 ? (
              <div className="p-10 text-center text-[var(--color-text-muted)]">当前流水线还没有步骤。</div>
            ) : (
              steps.map((step) => (
                <div key={step.id} className="p-5 flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <Badge variant="muted">#{step.sortOrder}</Badge>
                      <p className="text-sm font-semibold text-[var(--color-text)]">{step.name}</p>
                      <Badge variant="info">{step.stepTypeDescription}</Badge>
                      <Badge variant={step.enabled ? 'success' : 'warning'}>{step.enabled ? '启用' : '停用'}</Badge>
                    </div>
                    {step.command && (
                      <pre className="mt-3 text-xs text-[var(--color-text-muted)] bg-[var(--color-surface-2)] rounded-[var(--radius-md)] p-3 overflow-x-auto">
                        {step.command}
                      </pre>
                    )}
                    {step.configJson && <p className="text-xs text-[var(--color-text-subtle)] mt-2">配置 JSON: {step.configJson}</p>}
                  </div>
                  <div className="flex items-center gap-2 lg:flex-col lg:items-end">
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => {
                        setEditingStep(step)
                        setStepDialogOpen(true)
                      }}
                    >
                      编辑
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        if (confirm(`删除步骤“${step.name}”？`)) {
                          removeStepMutation.mutate(step.id)
                        }
                      }}
                    >
                      删除
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </SectionCard>

        <SectionCard className="p-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-lg font-semibold text-[var(--color-text)]">最近运行</h2>
              <p className="text-sm text-[var(--color-text-muted)] mt-1">进入独立运行详情页查看完整日志。</p>
            </div>
            <Button variant="secondary" size="sm" onClick={() => navigate('/pipelines?tab=runs')}>
              查看全部
            </Button>
          </div>
          <div className="mt-4 space-y-3">
            {isRunsLoading ? (
              Array.from({ length: 3 }).map((_, index) => (
                <div key={index} className="h-20 rounded-[var(--radius-lg)] bg-[var(--color-surface-2)] animate-pulse" />
              ))
            ) : runs.length === 0 ? (
              <div className="text-sm text-[var(--color-text-muted)]">暂无运行记录。</div>
            ) : (
              runs.slice(0, 6).map((run) => (
                <button
                  key={run.id}
                  type="button"
                  onClick={() => navigate(`/pipelines/runs/${run.id}`)}
                  className="w-full text-left rounded-[var(--radius-lg)] border border-[var(--color-border)] p-4 hover:bg-[var(--color-surface-2)] transition-colors"
                >
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <p className="text-sm font-medium text-[var(--color-text)]">{run.runNo}</p>
                      <p className="text-xs text-[var(--color-text-subtle)] mt-1">{formatDateTime(run.createdAt)}</p>
                    </div>
                    <Badge variant={pipelineStatusTone[run.status]}>{run.statusDescription}</Badge>
                  </div>
                  <div className="mt-3 flex items-center justify-between text-xs text-[var(--color-text-muted)]">
                    <span>{run.branchName || '未指定分支'}</span>
                    <span className="inline-flex items-center gap-1 text-[var(--color-primary)]">
                      查看详情
                      <ExternalLink size={12} />
                    </span>
                  </div>
                </button>
              ))
            )}
          </div>
        </SectionCard>
      </div>

      <PipelineFormDialog
        open={pipelineDialogOpen}
        onOpenChange={setPipelineDialogOpen}
        mode="edit"
        pipeline={pipeline}
        projects={projects}
        repositories={repositories}
      />
      <StepFormDialog
        open={stepDialogOpen}
        onOpenChange={setStepDialogOpen}
        pipeline={pipeline}
        step={editingStep}
        nextOrder={steps.length ? Math.max(...steps.map((step) => step.sortOrder)) + 1 : 1}
      />
      <RunPipelineDialog
        open={runDialogOpen}
        onOpenChange={setRunDialogOpen}
        pipeline={pipeline}
        versions={versions}
        onCreated={(runId) => navigate(`/pipelines/runs/${runId}`)}
      />
    </div>
  )
}