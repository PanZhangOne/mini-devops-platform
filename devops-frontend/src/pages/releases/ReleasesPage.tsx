import { useEffect, useMemo, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Plus,
  Rocket,
  Tag,
  GitBranch,
  GitCommit,
  Trash2,
  ChevronRight,
  Play,
  Workflow,
  Clock3,
  CircleDot,
} from 'lucide-react'
import { releasesApi } from '@/api/release'
import { projectsApi } from '@/api/work'
import { reposApi } from '@/api/code'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Dialog, DialogContent } from '@/components/ui/Dialog'
import type {
  PipelineEnv,
  PipelineRun,
  PipelineRunCreateRequest,
  PipelineRunStatus,
  PipelineRunStatusUpdateRequest,
  PipelineTriggerType,
  Version,
  VersionCreateRequest,
  VersionStatus,
} from '@/types'

// ─── Status Config ────────────────────────────────────────────────────────────

const STATUS_CONFIG: Record<
  VersionStatus,
  { label: string; className: string; next?: VersionStatus; nextLabel?: string }
> = {
  DRAFT: {
    label: '草稿',
    className: 'bg-slate-100 text-slate-600',
    next: 'READY',
    nextLabel: '提交待发布',
  },
  READY: {
    label: '待发布',
    className: 'bg-blue-50 text-blue-700',
    next: 'RELEASED',
    nextLabel: '发布',
  },
  RELEASED: {
    label: '已发布',
    className: 'bg-emerald-50 text-emerald-700',
    next: 'ROLLBACKED',
    nextLabel: '回滚',
  },
  ROLLBACKED: {
    label: '已回滚',
    className: 'bg-amber-50 text-amber-700',
  },
  CANCELLED: {
    label: '已取消',
    className: 'bg-gray-100 text-gray-400',
  },
}

// ─── Status Badge ─────────────────────────────────────────────────────────────

function StatusBadge({ status }: { status: VersionStatus }) {
  const cfg = STATUS_CONFIG[status]
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${cfg.className}`}>
      {cfg.label}
    </span>
  )
}

const PIPELINE_STATUS_CONFIG: Record<
  PipelineRunStatus,
  { label: string; className: string; tone: 'muted' | 'info' | 'success' | 'warning' | 'danger' }
> = {
  PENDING: {
    label: '等待中',
    className: 'bg-slate-100 text-slate-600',
    tone: 'muted',
  },
  RUNNING: {
    label: '运行中',
    className: 'bg-blue-50 text-blue-700',
    tone: 'info',
  },
  SUCCESS: {
    label: '成功',
    className: 'bg-emerald-50 text-emerald-700',
    tone: 'success',
  },
  FAILED: {
    label: '失败',
    className: 'bg-red-50 text-red-600',
    tone: 'danger',
  },
  CANCELLED: {
    label: '已取消',
    className: 'bg-amber-50 text-amber-700',
    tone: 'warning',
  },
}

const PIPELINE_ENV_OPTIONS: { value: PipelineEnv; label: string }[] = [
  { value: 'DEV', label: '开发环境' },
  { value: 'TEST', label: '测试环境' },
  { value: 'STAGING', label: '预发环境' },
  { value: 'PROD', label: '生产环境' },
]

const PIPELINE_TRIGGER_OPTIONS: { value: PipelineTriggerType; label: string }[] = [
  { value: 'MANUAL', label: '手动触发' },
  { value: 'WEBHOOK', label: 'Webhook 触发' },
  { value: 'SCHEDULED', label: '定时触发' },
]

const PIPELINE_STATUS_OPTIONS: { value: PipelineRunStatus; label: string }[] = [
  { value: 'PENDING', label: '等待中' },
  { value: 'RUNNING', label: '运行中' },
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAILED', label: '失败' },
  { value: 'CANCELLED', label: '已取消' },
]

function formatDateTime(value?: string) {
  if (!value) return '—'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatDuration(seconds?: number) {
  if (!seconds) return '—'
  if (seconds < 60) return `${seconds}s`
  const minutes = Math.floor(seconds / 60)
  const remain = seconds % 60
  return remain > 0 ? `${minutes}m ${remain}s` : `${minutes}m`
}

function PipelineStatusBadge({ status }: { status: PipelineRunStatus }) {
  const cfg = PIPELINE_STATUS_CONFIG[status]
  return <Badge variant={cfg.tone}>{cfg.label}</Badge>
}

// ─── Version Row ──────────────────────────────────────────────────────────────

function VersionRow({
  version,
  projectName,
  repoName,
  onStatusChange,
  onDelete,
}: {
  version: Version
  projectName: string
  repoName: string
  onStatusChange: (id: number, status: VersionStatus) => void
  onDelete: (id: number) => void
}) {
  const cfg = STATUS_CONFIG[version.status]

  return (
    <div className="px-6 py-4 flex items-center gap-4 hover:bg-[var(--color-surface-2)] transition-colors group">
      {/* Version info */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <span className="text-sm font-semibold text-[var(--color-text)] font-mono">
            {version.versionNo}
          </span>
          <StatusBadge status={version.status} />
        </div>
        <p className="text-sm text-[var(--color-text-muted)] truncate">{version.title}</p>
        <div className="flex items-center gap-3 mt-1.5">
          <span className="text-xs text-[var(--color-text-subtle)] bg-[var(--color-surface-3)] px-1.5 py-0.5 rounded">
            {projectName}
          </span>
          {version.gitTag && (
            <span className="flex items-center gap-1 text-xs text-[var(--color-text-subtle)]">
              <Tag size={10} />
              {version.gitTag}
            </span>
          )}
          {version.branchName && (
            <span className="flex items-center gap-1 text-xs text-[var(--color-text-subtle)]">
              <GitBranch size={10} />
              {version.branchName}
            </span>
          )}
          {version.commitHash && (
            <span className="flex items-center gap-1 text-xs text-[var(--color-text-subtle)] font-mono">
              <GitCommit size={10} />
              {version.commitHash.slice(0, 7)}
            </span>
          )}
          {repoName && (
            <span className="text-xs text-[var(--color-text-subtle)]">{repoName}</span>
          )}
        </div>
      </div>

      {/* Release time */}
      <div className="text-right hidden sm:block">
        {version.releasedAt && (
          <p className="text-xs text-[var(--color-text-subtle)]">
            {new Date(version.releasedAt).toLocaleDateString('zh-CN')}
          </p>
        )}
        <p className="text-xs text-[var(--color-text-subtle)]">
          {new Date(version.createdAt).toLocaleDateString('zh-CN')}
        </p>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
        {cfg.next && (
          <Button
            variant="secondary"
            size="sm"
            onClick={() => onStatusChange(version.id, cfg.next!)}
            className="whitespace-nowrap"
          >
            {cfg.nextLabel}
            <ChevronRight size={12} />
          </Button>
        )}
        {version.status !== 'CANCELLED' && version.status !== 'RELEASED' && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onStatusChange(version.id, 'CANCELLED')}
            className="text-[var(--color-text-subtle)] hover:text-[var(--color-danger)]"
          >
            取消
          </Button>
        )}
        <button
          className="p-1.5 rounded-[var(--radius-sm)] hover:bg-red-50 text-[var(--color-text-subtle)] hover:text-red-500 transition-colors"
          onClick={() => {
            if (confirm(`删除版本 "${version.versionNo}"？`)) onDelete(version.id)
          }}
        >
          <Trash2 size={13} />
        </button>
      </div>
    </div>
  )
}

// ─── Create Modal ─────────────────────────────────────────────────────────────

const defaultForm: VersionCreateRequest = {
  projectId: 0,
  repositoryId: 0,
  versionNo: '',
  title: '',
  gitTag: '',
  branchName: '',
  commitHash: '',
  description: '',
}

const defaultPipelineForm: PipelineRunCreateRequest = {
  projectId: 0,
  repositoryId: 0,
  versionId: 0,
  env: 'DEV',
  imageTag: '',
  commitHash: '',
  triggerType: 'MANUAL',
}

function CreateVersionModal({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (v: boolean) => void
}) {
  const qc = useQueryClient()
  const [form, setForm] = useState<VersionCreateRequest>(defaultForm)
  const [formError, setFormError] = useState('')

  const { data: projects = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (d) => d ?? [],
  })

  const { data: repos = [] } = useQuery({
    queryKey: ['repos-for-project', form.projectId],
    queryFn: () => reposApi.list(form.projectId),
    enabled: form.projectId > 0,
    select: (d) => d ?? [],
  })

  const createMutation = useMutation({
    mutationFn: releasesApi.create,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['releases'] })
      onOpenChange(false)
      setForm(defaultForm)
      setFormError('')
    },
    onError: (err: unknown) => {
      const msg =
        (err as Error).message ??
        '创建失败，请重试。'
      setFormError(msg)
    },
  })

  const set =
    <K extends keyof VersionCreateRequest>(key: K) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) =>
      setForm((f) => ({ ...f, [key]: key === 'projectId' || key === 'repositoryId' ? Number(e.target.value) : e.target.value }))

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent title="新建版本">
        <form
          onSubmit={(e) => {
            e.preventDefault()
            setFormError('')
            createMutation.mutate(form)
          }}
          className="space-y-4"
        >
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="项目"
              value={String(form.projectId || '')}
              onChange={set('projectId')}
              options={[
                { value: '0', label: '选择项目...' },
                ...projects.map((p) => ({ value: String(p.id), label: p.name })),
              ]}
            />
            <Select
              label="代码仓库"
              value={String(form.repositoryId || '')}
              onChange={set('repositoryId')}
              options={[
                { value: '0', label: form.projectId > 0 ? '选择仓库...' : '先选择项目' },
                ...repos.map((r) => ({ value: String(r.id), label: r.repoName })),
              ]}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="版本号"
              value={form.versionNo}
              onChange={set('versionNo')}
              placeholder="v1.0.0"
              required
            />
            <Input
              label="版本标题"
              value={form.title}
              onChange={set('title')}
              placeholder="功能发布"
              required
            />
          </div>
          <div className="grid grid-cols-3 gap-3">
            <Input
              label="Git Tag"
              value={form.gitTag}
              onChange={set('gitTag')}
              placeholder="v1.0.0"
            />
            <Input
              label="分支"
              value={form.branchName}
              onChange={set('branchName')}
              placeholder="main"
            />
            <Input
              label="Commit Hash"
              value={form.commitHash}
              onChange={set('commitHash')}
              placeholder="abc1234"
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-[var(--color-text)]">描述</label>
            <textarea
              value={form.description}
              onChange={set('description')}
              rows={3}
              placeholder="版本描述（可选）"
              className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
            />
          </div>
          {formError && (
            <p className="text-sm text-[var(--color-danger)] bg-red-50 px-3 py-2 rounded-[var(--radius-md)]">
              {formError}
            </p>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button
              type="submit"
              loading={createMutation.isPending}
              disabled={!form.projectId || !form.repositoryId}
            >
              创建
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function CreatePipelineRunModal({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (value: boolean) => void
}) {
  const qc = useQueryClient()
  const [form, setForm] = useState<PipelineRunCreateRequest>(defaultPipelineForm)
  const [formError, setFormError] = useState('')

  const { data: projects = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (d) => d ?? [],
  })

  const { data: repos = [] } = useQuery({
    queryKey: ['repos-for-pipeline-project', form.projectId],
    queryFn: () => reposApi.list(form.projectId),
    enabled: form.projectId > 0,
    select: (d) => d ?? [],
  })

  const { data: versions = [] } = useQuery({
    queryKey: ['versions-for-pipeline-project', form.projectId],
    queryFn: () => releasesApi.list(form.projectId),
    enabled: form.projectId > 0,
    select: (d) => d ?? [],
  })

  const versionOptions = versions.filter((item) => !form.repositoryId || item.repositoryId === form.repositoryId)

  const createMutation = useMutation({
    mutationFn: releasesApi.createPipelineRun,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['pipeline-runs'] })
      onOpenChange(false)
      setForm(defaultPipelineForm)
      setFormError('')
    },
    onError: (err: unknown) => {
      const msg =
        (err as Error).message ??
        '创建流水线失败，请重试。'
      setFormError(msg)
    },
  })

  const set =
    <K extends keyof PipelineRunCreateRequest>(key: K) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      const value = e.target.value
      setForm((current) => {
        if (key === 'projectId') {
          return {
            ...current,
            projectId: Number(value),
            repositoryId: 0,
            versionId: 0,
          }
        }
        if (key === 'repositoryId') {
          return {
            ...current,
            repositoryId: Number(value),
            versionId: 0,
          }
        }
        if (key === 'versionId') {
          return {
            ...current,
            versionId: Number(value),
          }
        }
        return {
          ...current,
          [key]: value,
        }
      })
    }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent title="创建流水线执行">
        <form
          onSubmit={(e) => {
            e.preventDefault()
            setFormError('')
            createMutation.mutate(form)
          }}
          className="space-y-4"
        >
          <div className="grid grid-cols-3 gap-3">
            <Select
              label="项目"
              value={String(form.projectId || '0')}
              onChange={set('projectId')}
              options={[
                { value: '0', label: '选择项目...' },
                ...projects.map((p) => ({ value: String(p.id), label: p.name })),
              ]}
            />
            <Select
              label="仓库"
              value={String(form.repositoryId || '0')}
              onChange={set('repositoryId')}
              options={[
                { value: '0', label: form.projectId ? '选择仓库...' : '先选择项目' },
                ...repos.map((r) => ({ value: String(r.id), label: r.repoName })),
              ]}
            />
            <Select
              label="版本"
              value={String(form.versionId || '0')}
              onChange={set('versionId')}
              options={[
                { value: '0', label: form.repositoryId ? '选择版本...' : '先选择仓库' },
                ...versionOptions.map((v) => ({ value: String(v.id), label: `${v.versionNo} · ${v.title}` })),
              ]}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="环境"
              value={form.env}
              onChange={set('env')}
              options={PIPELINE_ENV_OPTIONS}
            />
            <Select
              label="触发方式"
              value={form.triggerType}
              onChange={set('triggerType')}
              options={PIPELINE_TRIGGER_OPTIONS}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="镜像标签"
              value={form.imageTag}
              onChange={set('imageTag')}
              placeholder="registry/app:v1.0.0"
            />
            <Input
              label="Commit Hash"
              value={form.commitHash}
              onChange={set('commitHash')}
              placeholder="abc1234"
            />
          </div>
          {formError && (
            <p className="text-sm text-[var(--color-danger)] bg-red-50 px-3 py-2 rounded-[var(--radius-md)]">
              {formError}
            </p>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button
              type="submit"
              loading={createMutation.isPending}
              disabled={!form.projectId || !form.repositoryId || !form.versionId}
            >
              创建执行
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function UpdatePipelineRunStatusModal({
  run,
  onOpenChange,
}: {
  run: PipelineRun | null
  onOpenChange: (value: boolean) => void
}) {
  const qc = useQueryClient()
  const [form, setForm] = useState<PipelineRunStatusUpdateRequest>({
    status: 'RUNNING',
    imageTag: '',
    logText: '',
    errorMessage: '',
  })

  useEffect(() => {
    if (!run) return
    setForm({
      status: run.status,
      imageTag: run.imageTag ?? '',
      logText: run.logText ?? '',
      errorMessage: run.errorMessage ?? '',
    })
  }, [run])

  const updateMutation = useMutation({
    mutationFn: (payload: PipelineRunStatusUpdateRequest) => {
      if (!run) {
        throw new Error('missing pipeline run')
      }
      return releasesApi.updatePipelineRunStatus(run.id, payload)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['pipeline-runs'] })
      onOpenChange(false)
    },
  })

  return (
    <Dialog open={!!run} onOpenChange={onOpenChange}>
      <DialogContent title={run ? `更新执行状态 · ${run.runNo}` : '更新执行状态'}>
        <form
          onSubmit={(e) => {
            e.preventDefault()
            updateMutation.mutate(form)
          }}
          className="space-y-4"
        >
          <Select
            label="状态"
            value={form.status}
            onChange={(e) => setForm((current) => ({ ...current, status: e.target.value as PipelineRunStatus }))}
            options={PIPELINE_STATUS_OPTIONS}
          />
          <Input
            label="镜像标签"
            value={form.imageTag}
            onChange={(e) => setForm((current) => ({ ...current, imageTag: e.target.value }))}
            placeholder="可选"
          />
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-[var(--color-text)]">日志</label>
            <textarea
              value={form.logText}
              onChange={(e) => setForm((current) => ({ ...current, logText: e.target.value }))}
              rows={5}
              placeholder="可选，补充执行日志"
              className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-[var(--color-text)]">错误信息</label>
            <textarea
              value={form.errorMessage}
              onChange={(e) => setForm((current) => ({ ...current, errorMessage: e.target.value }))}
              rows={3}
              placeholder="可选，失败时填写"
              className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button type="submit" loading={updateMutation.isPending}>
              保存状态
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function VersionsPanel({ onCreate }: { onCreate: () => void }) {
  const qc = useQueryClient()
  const [filterProjectId, setFilterProjectId] = useState<number | undefined>(undefined)
  const [filterStatus, setFilterStatus] = useState<VersionStatus | ''>('')

  const { data: projects = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (d) => d ?? [],
  })

  const { data: versions = [], isLoading } = useQuery({
    queryKey: ['releases', filterProjectId],
    queryFn: () => releasesApi.list(filterProjectId),
    select: (d) => d ?? [],
  })

  const { data: allRepos = [] } = useQuery({
    queryKey: ['repositories'],
    queryFn: () => reposApi.list(),
    select: (d) => d ?? [],
  })

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: VersionStatus }) =>
      releasesApi.updateStatus(id, { status }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['releases'] }),
  })

  const deleteMutation = useMutation({
    mutationFn: releasesApi.remove,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['releases'] }),
  })

  const projectMap = Object.fromEntries(projects.map((p) => [p.id, p.name]))
  const repoMap = Object.fromEntries(allRepos.map((r) => [r.id, r.repoName]))

  const filtered = versions.filter((v) => !filterStatus || v.status === filterStatus)

  const stats: Record<VersionStatus, number> = {
    DRAFT: 0,
    READY: 0,
    RELEASED: 0,
    ROLLBACKED: 0,
    CANCELLED: 0,
  }
  versions.forEach((v) => {
    stats[v.status] = (stats[v.status] ?? 0) + 1
  })

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-semibold text-[var(--color-text)]">Versions</h2>
          <p className="text-sm text-[var(--color-text-muted)] mt-0.5">版本管理与发布追踪</p>
        </div>
        <Button onClick={onCreate}>
          <Plus size={15} />
          新建版本
        </Button>
      </div>

      <div className="flex items-center gap-3 mb-6 flex-wrap">
        {(Object.entries(stats) as [VersionStatus, number][])
          .filter(([, count]) => count > 0)
          .map(([status, count]) => (
            <button
              key={status}
              onClick={() => setFilterStatus((prev) => (prev === status ? '' : status))}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium border transition-all ${
                filterStatus === status
                  ? 'border-[var(--color-primary)] bg-[var(--color-primary-light)] text-[var(--color-primary)]'
                  : `border-transparent ${STATUS_CONFIG[status].className}`
              }`}
            >
              {STATUS_CONFIG[status].label}
              <span className="bg-white/60 px-1 py-0.5 rounded-full">{count}</span>
            </button>
          ))}
      </div>

      <div className="flex items-center gap-3 mb-4">
        <Select
          value={String(filterProjectId ?? '')}
          onChange={(e) => setFilterProjectId(e.target.value ? Number(e.target.value) : undefined)}
          options={[
            { value: '', label: '全部项目' },
            ...projects.map((p) => ({ value: String(p.id), label: p.name })),
          ]}
          className="w-44"
        />
        {(filterStatus || filterProjectId) && (
          <button
            className="text-xs text-[var(--color-text-muted)] hover:text-[var(--color-danger)] transition-colors"
            onClick={() => {
              setFilterStatus('')
              setFilterProjectId(undefined)
            }}
          >
            清除筛选
          </button>
        )}
      </div>

      <div className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)] overflow-hidden">
        {isLoading ? (
          <div className="divide-y divide-[var(--color-border)]">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="px-6 py-4 h-20 animate-pulse" />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <Rocket size={40} className="text-[var(--color-border)] mb-3" />
            <p className="text-[var(--color-text-muted)] font-medium">
              {versions.length === 0 ? '还没有版本记录' : '没有符合筛选条件的版本'}
            </p>
            {versions.length === 0 && (
              <p className="text-sm text-[var(--color-text-subtle)] mt-1">点击「新建版本」开始管理你的发布。</p>
            )}
          </div>
        ) : (
          <div className="divide-y divide-[var(--color-border)]">
            {filtered.map((v) => (
              <VersionRow
                key={v.id}
                version={v}
                projectName={projectMap[v.projectId] ?? `Project #${v.projectId}`}
                repoName={repoMap[v.repositoryId] ?? ''}
                onStatusChange={(id, status) => updateStatusMutation.mutate({ id, status })}
                onDelete={(id) => deleteMutation.mutate(id)}
              />
            ))}
          </div>
        )}
      </div>

      <div className="mt-4 flex items-center gap-2 text-xs text-[var(--color-text-subtle)]">
        <span>状态流转：</span>
        {(['DRAFT', 'READY', 'RELEASED'] as VersionStatus[]).map((s, i) => (
          <span key={s} className="flex items-center gap-1">
            {i > 0 && <ChevronRight size={10} />}
            <StatusBadge status={s} />
          </span>
        ))}
        <span className="ml-2 opacity-60">（可随时取消或回滚）</span>
      </div>
    </div>
  )
}

function PipelineRunRow({
  run,
  projectName,
  repositoryName,
  versionLabel,
  onUpdate,
  onDelete,
}: {
  run: PipelineRun
  projectName: string
  repositoryName: string
  versionLabel: string
  onUpdate: (run: PipelineRun) => void
  onDelete: (id: number) => void
}) {
  return (
    <div className="px-6 py-4 flex items-start gap-4 hover:bg-[var(--color-surface-2)] transition-colors group">
      <div className="w-10 h-10 rounded-[var(--radius-md)] bg-[var(--color-primary-light)] text-[var(--color-primary)] flex items-center justify-center flex-shrink-0">
        <Workflow size={18} />
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1 flex-wrap">
          <span className="text-sm font-semibold text-[var(--color-text)] font-mono">{run.runNo}</span>
          <PipelineStatusBadge status={run.status} />
          <Badge variant="muted">{run.envDescription || run.env}</Badge>
          <Badge variant="info">{run.triggerTypeDescription || run.triggerType}</Badge>
        </div>
        <div className="text-sm text-[var(--color-text-muted)] flex items-center gap-2 flex-wrap">
          <span>{projectName}</span>
          <span className="text-[var(--color-text-subtle)]">/</span>
          <span>{repositoryName}</span>
          <span className="text-[var(--color-text-subtle)]">/</span>
          <span>{versionLabel}</span>
        </div>
        <div className="mt-2 flex items-center gap-4 text-xs text-[var(--color-text-subtle)] flex-wrap">
          <span className="flex items-center gap-1">
            <Clock3 size={11} />
            {formatDateTime(run.startedAt || run.createdAt)}
          </span>
          <span>耗时 {formatDuration(run.durationSeconds)}</span>
          {run.imageTag && <span className="font-mono">{run.imageTag}</span>}
          {run.commitHash && <span className="font-mono">{run.commitHash.slice(0, 7)}</span>}
        </div>
        {(run.errorMessage || run.logText) && (
          <div className="mt-2 rounded-[var(--radius-md)] bg-[var(--color-surface-2)] border border-[var(--color-border)] p-3 text-xs text-[var(--color-text-muted)]">
            {run.errorMessage && <p className="text-[var(--color-danger)] mb-1">{run.errorMessage}</p>}
            {run.logText && <p className="line-clamp-2 whitespace-pre-wrap">{run.logText}</p>}
          </div>
        )}
      </div>
      <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
        <Button variant="secondary" size="sm" onClick={() => onUpdate(run)}>
          更新状态
        </Button>
        <button
          className="p-1.5 rounded-[var(--radius-sm)] hover:bg-red-50 text-[var(--color-text-subtle)] hover:text-red-500 transition-colors"
          onClick={() => {
            if (confirm(`删除流水线执行 "${run.runNo}"？`)) onDelete(run.id)
          }}
        >
          <Trash2 size={13} />
        </button>
      </div>
    </div>
  )
}

function PipelinesPanel({ onCreate }: { onCreate: () => void }) {
  const qc = useQueryClient()
  const [editingRun, setEditingRun] = useState<PipelineRun | null>(null)
  const [projectId, setProjectId] = useState<number | undefined>(undefined)
  const [env, setEnv] = useState<PipelineEnv | ''>('')
  const [status, setStatus] = useState<PipelineRunStatus | ''>('')

  const queryParams = useMemo(
    () => ({
      projectId,
      env: env || undefined,
      status: status || undefined,
    }),
    [env, projectId, status],
  )

  const { data: projects = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (d) => d ?? [],
  })

  const { data: repositories = [] } = useQuery({
    queryKey: ['repositories'],
    queryFn: () => reposApi.list(),
    select: (d) => d ?? [],
  })

  const { data: versions = [] } = useQuery({
    queryKey: ['release-version-map'],
    queryFn: () => releasesApi.list(),
    select: (d) => d ?? [],
  })

  const { data: runs = [], isLoading } = useQuery({
    queryKey: ['pipeline-runs', queryParams],
    queryFn: () => releasesApi.listPipelineRuns(queryParams),
    select: (d) => d ?? [],
  })

  const deleteMutation = useMutation({
    mutationFn: releasesApi.removePipelineRun,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['pipeline-runs'] }),
  })

  const projectMap = Object.fromEntries(projects.map((item) => [item.id, item.name]))
  const repositoryMap = Object.fromEntries(repositories.map((item) => [item.id, item.repoName]))
  const versionMap = Object.fromEntries(versions.map((item) => [item.id, `${item.versionNo} · ${item.title}`]))

  const runStats: Record<PipelineRunStatus, number> = {
    PENDING: 0,
    RUNNING: 0,
    SUCCESS: 0,
    FAILED: 0,
    CANCELLED: 0,
  }
  runs.forEach((item) => {
    runStats[item.status] = (runStats[item.status] ?? 0) + 1
  })

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-semibold text-[var(--color-text)]">Pipelines</h2>
          <p className="text-sm text-[var(--color-text-muted)] mt-0.5">对接后端 PipelineRunController 的执行记录与状态管理</p>
        </div>
        <Button onClick={onCreate}>
          <Play size={15} />
          创建执行
        </Button>
      </div>

      <div className="grid grid-cols-2 xl:grid-cols-5 gap-3 mb-6">
        {(Object.entries(runStats) as [PipelineRunStatus, number][]).map(([key, value]) => (
          <button
            key={key}
            onClick={() => setStatus((current) => (current === key ? '' : key))}
            className={`rounded-[var(--radius-lg)] border p-4 text-left transition-colors ${
              status === key
                ? 'border-[var(--color-primary)] bg-[var(--color-primary-light)]'
                : 'border-[var(--color-border)] bg-[var(--color-surface)]'
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <PipelineStatusBadge status={key} />
              <CircleDot size={14} className="text-[var(--color-text-subtle)]" />
            </div>
            <div className="text-2xl font-semibold text-[var(--color-text)]">{value}</div>
          </button>
        ))}
      </div>

      <div className="flex items-center gap-3 mb-4 flex-wrap">
        <Select
          value={String(projectId ?? '')}
          onChange={(e) => setProjectId(e.target.value ? Number(e.target.value) : undefined)}
          options={[
            { value: '', label: '全部项目' },
            ...projects.map((p) => ({ value: String(p.id), label: p.name })),
          ]}
          className="w-44"
        />
        <Select
          value={env}
          onChange={(e) => setEnv(e.target.value as PipelineEnv | '')}
          options={[{ value: '', label: '全部环境' }, ...PIPELINE_ENV_OPTIONS]}
          className="w-40"
        />
        {(projectId || env || status) && (
          <button
            className="text-xs text-[var(--color-text-muted)] hover:text-[var(--color-danger)] transition-colors"
            onClick={() => {
              setProjectId(undefined)
              setEnv('')
              setStatus('')
            }}
          >
            清除筛选
          </button>
        )}
      </div>

      <div className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)] overflow-hidden">
        {isLoading ? (
          <div className="divide-y divide-[var(--color-border)]">
            {Array.from({ length: 4 }).map((_, index) => (
              <div key={index} className="px-6 py-4 h-28 animate-pulse" />
            ))}
          </div>
        ) : runs.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <Workflow size={40} className="text-[var(--color-border)] mb-3" />
            <p className="text-[var(--color-text-muted)] font-medium">还没有流水线执行记录</p>
            <p className="text-sm text-[var(--color-text-subtle)] mt-1">点击「创建执行」开始记录你的发布流程。</p>
          </div>
        ) : (
          <div className="divide-y divide-[var(--color-border)]">
            {runs.map((run) => (
              <PipelineRunRow
                key={run.id}
                run={run}
                projectName={projectMap[run.projectId] ?? `Project #${run.projectId}`}
                repositoryName={repositoryMap[run.repositoryId] ?? `Repo #${run.repositoryId}`}
                versionLabel={versionMap[run.versionId] ?? `Version #${run.versionId}`}
                onUpdate={setEditingRun}
                onDelete={(id) => deleteMutation.mutate(id)}
              />
            ))}
          </div>
        )}
      </div>

      <div className="mt-4 flex items-center gap-2 text-xs text-[var(--color-text-subtle)] flex-wrap">
        <span>执行状态：</span>
        {(['PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED'] as PipelineRunStatus[]).map((item) => (
          <PipelineStatusBadge key={item} status={item} />
        ))}
      </div>

      <UpdatePipelineRunStatusModal run={editingRun} onOpenChange={(value) => !value && setEditingRun(null)} />
    </div>
  )
}

// ─── Main Page ────────────────────────────────────────────────────────────────

export function ReleasesPage({ defaultTab = 'versions' }: { defaultTab?: 'versions' | 'pipelines' }) {
  const [createOpen, setCreateOpen] = useState(false)
  const [createPipelineOpen, setCreatePipelineOpen] = useState(false)
  const [activeTab, setActiveTab] = useState<'versions' | 'pipelines'>(defaultTab)

  useEffect(() => {
    setActiveTab(defaultTab)
  }, [defaultTab])

  return (
    <div className="p-8 max-w-6xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-[var(--color-text)]">Release Center</h1>
        <p className="text-sm text-[var(--color-text-muted)] mt-1">统一管理版本发布和流水线执行记录</p>
      </div>

      {activeTab === 'versions' ? (
        <VersionsPanel onCreate={() => setCreateOpen(true)} />
      ) : (
        <PipelinesPanel onCreate={() => setCreatePipelineOpen(true)} />
      )}

      <CreateVersionModal open={createOpen} onOpenChange={setCreateOpen} />
      <CreatePipelineRunModal open={createPipelineOpen} onOpenChange={setCreatePipelineOpen} />
    </div>
  )
}
