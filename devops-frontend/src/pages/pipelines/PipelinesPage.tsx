import {useEffect, useMemo, useState} from 'react'
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query'
import {useNavigate} from 'react-router-dom'
import {
    Activity,
    CircleOff,
    ExternalLink,
    Play,
    Plus,
    RefreshCw,
    Rocket,
    Server,
    Settings2,
    Terminal,
    Trash2,
    Workflow,
} from 'lucide-react'
import {pipelineApi} from '@/api/pipeline'
import {reposApi} from '@/api/code'
import {releasesApi} from '@/api/release'
import {projectsApi} from '@/api/work'
import {Badge} from '@/components/ui/Badge'
import {Button} from '@/components/ui/Button'
import {Dialog, DialogContent} from '@/components/ui/Dialog'
import {Input} from '@/components/ui/Input'
import {Select} from '@/components/ui/Select'
import type {
    PipelineDefinition,
    PipelineDefinitionCreateRequest,
    PipelineDefinitionUpdateRequest,
    PipelineEnv,
    PipelineRunRecordCreateRequest,
    PipelineRunRecordQueryRequest,
    PipelineRunStatus,
    PipelineStepDefinition,
    PipelineStepDefinitionCreateRequest,
    PipelineStepDefinitionUpdateRequest,
    PipelineStepType,
    PipelineTriggerType,
    RunnerRecord,
    RunnerStatus,
    RunnerStatusUpdatePayload,
    Version,
} from '@/types'

const pipelineTriggerOptions: { value: PipelineTriggerType; label: string }[] = [
    {value: 'MANUAL', label: '手动触发'},
    {value: 'WEBHOOK', label: 'Webhook 触发'},
    {value: 'SCHEDULED', label: '定时触发'},
]

const pipelineStepTypeOptions: { value: PipelineStepType; label: string }[] = [
    {value: 'SHELL', label: 'Shell 命令'},
    {value: "GIT_CLONE", label: "Git Clone"},
    {value: 'MAVEN_BUILD', label: 'Maven 构建'},
    {value: 'DOCKER_BUILD', label: 'Docker 构建'},
    {value: 'DOCKER_PUSH', label: 'Docker 推送'},
    {value: 'DOCKER_DEPLOY', label: 'Docker 部署'},
    {value: 'HTTP_CHECK', label: 'HTTP 健康检查'},
]

const envOptions: { value: PipelineEnv; label: string }[] = [
    {value: 'DEV', label: '开发环境'},
    {value: 'TEST', label: '测试环境'},
    {value: 'STAGING', label: '预发环境'},
    {value: 'PROD', label: '生产环境'},
]

export const pipelineStatusTone: Record<PipelineRunStatus, 'muted' | 'info' | 'success' | 'danger' | 'warning'> = {
    PENDING: 'muted',
    RUNNING: 'info',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'warning',
}

const runnerStatusTone: Record<RunnerStatus, 'success' | 'muted' | 'warning' | 'danger'> = {
    ONLINE: 'success',
    OFFLINE: 'muted',
    BUSY: 'warning',
    DISABLED: 'danger',
}

const runnerStatusOptions: { value: RunnerStatus; label: string }[] = [
    {value: 'ONLINE', label: '在线'},
    {value: 'OFFLINE', label: '离线'},
    {value: 'BUSY', label: '忙碌'},
    {value: 'DISABLED', label: '禁用'},
]

type PipelineTab = 'definitions' | 'runs' | 'runners'

type PipelineFormState = {
    projectId: string
    repositoryId: string
    name: string
    code: string
    description: string
    triggerType: PipelineTriggerType
    enabled: boolean
}

type StepFormState = {
    name: string
    stepType: PipelineStepType
    sortOrder: string
    command: string
    configJson: string
    enabled: boolean
}

type RunFormState = {
    versionId: string
    branchName: string
    commitHash: string
    imageTag: string
    env: PipelineEnv | ''
}

const defaultPipelineForm: PipelineFormState = {
    projectId: '',
    repositoryId: '',
    name: '',
    code: '',
    description: '',
    triggerType: 'MANUAL',
    enabled: true,
}

const defaultStepForm: StepFormState = {
    name: '',
    stepType: 'SHELL',
    sortOrder: '1',
    command: '',
    configJson: '',
    enabled: true,
}

const defaultRunForm: RunFormState = {
    versionId: '',
    branchName: '',
    commitHash: '',
    imageTag: '',
    env: 'DEV',
}

export function formatDateTime(value?: string) {
    if (!value) return '—'
    return new Date(value).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    })
}

export function formatDuration(seconds?: number) {
    if (!seconds) return '—'
    if (seconds < 60) return `${seconds}s`
    const minutes = Math.floor(seconds / 60)
    const remainSeconds = seconds % 60
    return remainSeconds ? `${minutes}m ${remainSeconds}s` : `${minutes}m`
}

function emptyToUndefined(value: string) {
    return value.trim() ? value.trim() : undefined
}

export function FieldLabel({children}: { children: React.ReactNode }) {
    return <span className="text-xs uppercase tracking-wide text-[var(--color-text-subtle)]">{children}</span>
}

export function SectionCard({children, className = ''}: { children: React.ReactNode; className?: string }) {
    return (
        <section
            className={`bg-[var(--color-surface)] border border-[var(--color-border)] rounded-[var(--radius-xl)] ${className}`}>
            {children}
        </section>
    )
}

export function PipelineFormDialog({
                                       open,
                                       onOpenChange,
                                       mode,
                                       pipeline,
                                       projects,
                                       repositories,
                                       onCreated,
                                   }: {
    open: boolean
    onOpenChange: (open: boolean) => void
    mode: 'create' | 'edit'
    pipeline: PipelineDefinition | null
    projects: { id: number; name: string }[]
    repositories: { id: number; projectId: number; repoName: string }[]
    onCreated?: (pipeline: PipelineDefinition) => void
}) {
    const qc = useQueryClient()
    const [form, setForm] = useState<PipelineFormState>(defaultPipelineForm)
    const [formError, setFormError] = useState('')

    useEffect(() => {
        if (!open) return
        if (mode === 'edit' && pipeline) {
            setForm({
                projectId: String(pipeline.projectId),
                repositoryId: String(pipeline.repositoryId),
                name: pipeline.name,
                code: pipeline.code,
                description: pipeline.description ?? '',
                triggerType: pipeline.triggerType,
                enabled: pipeline.enabled,
            })
            return
        }
        setForm(defaultPipelineForm)
        setFormError('')
    }, [mode, open, pipeline])

    const filteredRepos = useMemo(
        () => repositories.filter((repo) => !form.projectId || repo.projectId === Number(form.projectId)),
        [repositories, form.projectId],
    )

    const createMutation = useMutation({
        mutationFn: (payload: PipelineDefinitionCreateRequest) => pipelineApi.createPipeline(payload),
        onSuccess: (created) => {
            qc.invalidateQueries({queryKey: ['pipelines']})
            qc.invalidateQueries({queryKey: ['pipeline-runs']})
            onOpenChange(false)
            setForm(defaultPipelineForm)
            onCreated?.(created)
        },
        onError: (error: unknown) => {
            setFormError(
                (error as Error).message ??
                '创建流水线失败',
            )
        },
    })

    const updateMutation = useMutation({
        mutationFn: (payload: PipelineDefinitionUpdateRequest) => {
            if (!pipeline) throw new Error('missing pipeline')
            return pipelineApi.updatePipeline(pipeline.id, payload)
        },
        onSuccess: () => {
            qc.invalidateQueries({queryKey: ['pipelines']})
            onOpenChange(false)
        },
        onError: (error: unknown) => {
            setFormError(
                (error as Error).message ??
                '更新流水线失败',
            )
        },
    })

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault()
        setFormError('')

        if (mode === 'create') {
            if (!form.projectId || !form.repositoryId) {
                setFormError('请先选择项目和仓库')
                return
            }
            createMutation.mutate({
                projectId: Number(form.projectId),
                repositoryId: Number(form.repositoryId),
                name: form.name.trim(),
                code: form.code.trim(),
                description: emptyToUndefined(form.description),
                triggerType: form.triggerType,
            })
            return
        }

        updateMutation.mutate({
            name: form.name.trim(),
            description: emptyToUndefined(form.description),
            repositoryId: Number(form.repositoryId),
            triggerType: form.triggerType,
            enabled: form.enabled,
        })
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent
                title={mode === 'create' ? '新建流水线' : '编辑流水线'}
                description={mode === 'create' ? '配置基础信息后即可继续编排步骤。' : '更新流水线名称、触发策略和启用状态。'}
            >
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="grid grid-cols-2 gap-3">
                        <Select
                            label="项目"
                            value={form.projectId}
                            onChange={(event) => setForm((current) => ({
                                ...current,
                                projectId: event.target.value,
                                repositoryId: ''
                            }))}
                            disabled={mode === 'edit'}
                            options={[
                                {value: '', label: '选择项目'},
                                ...projects.map((project) => ({value: String(project.id), label: project.name})),
                            ]}
                        />
                        <Select
                            label="仓库"
                            value={form.repositoryId}
                            onChange={(event) => setForm((current) => ({...current, repositoryId: event.target.value}))}
                            options={[
                                {value: '', label: '选择仓库'},
                                ...filteredRepos.map((repo) => ({value: String(repo.id), label: repo.repoName})),
                            ]}
                        />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <Input
                            label="流水线名称"
                            value={form.name}
                            onChange={(event) => setForm((current) => ({...current, name: event.target.value}))}
                            required
                        />
                        <Input
                            label="流水线编码"
                            value={form.code}
                            onChange={(event) => setForm((current) => ({...current, code: event.target.value}))}
                            disabled={mode === 'edit'}
                            required
                        />
                    </div>
                    <Select
                        label="触发方式"
                        value={form.triggerType}
                        onChange={(event) => setForm((current) => ({
                            ...current,
                            triggerType: event.target.value as PipelineTriggerType
                        }))}
                        options={pipelineTriggerOptions}
                    />
                    <div
                        className="flex items-center justify-between rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2">
                        <div>
                            <p className="text-sm font-medium text-[var(--color-text)]">启用流水线</p>
                            <p className="text-xs text-[var(--color-text-muted)]">禁用后无法创建新的运行任务</p>
                        </div>
                        <input
                            type="checkbox"
                            checked={form.enabled}
                            onChange={(event) => setForm((current) => ({...current, enabled: event.target.checked}))}
                            disabled={mode === 'create'}
                            className="h-4 w-4 rounded border-[var(--color-border)]"
                        />
                    </div>
                    <div className="flex flex-col gap-1.5">
                        <label className="text-sm font-medium text-[var(--color-text)]">描述</label>
                        <textarea
                            value={form.description}
                            onChange={(event) => setForm((current) => ({...current, description: event.target.value}))}
                            rows={4}
                            className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)]"
                        />
                    </div>
                    {formError && (
                        <p className="text-sm text-[var(--color-danger)] bg-red-50 px-3 py-2 rounded-[var(--radius-md)]">{formError}</p>
                    )}
                    <div className="flex justify-end gap-2 pt-2">
                        <Button variant="secondary" type="button" onClick={() => onOpenChange(false)}>
                            取消
                        </Button>
                        <Button type="submit" loading={createMutation.isPending || updateMutation.isPending}>
                            {mode === 'create' ? '创建流水线' : '保存修改'}
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    )
}

export function StepFormDialog({
                                   open,
                                   onOpenChange,
                                   pipeline,
                                   step,
                                   nextOrder,
                               }: {
    open: boolean
    onOpenChange: (open: boolean) => void
    pipeline: PipelineDefinition | null
    step: PipelineStepDefinition | null
    nextOrder: number
}) {
    const qc = useQueryClient()
    const [form, setForm] = useState<StepFormState>(defaultStepForm)
    const [formError, setFormError] = useState('')

    useEffect(() => {
        if (!open) return
        if (step) {
            setForm({
                name: step.name,
                stepType: step.stepType,
                sortOrder: String(step.sortOrder),
                command: step.command ?? '',
                configJson: step.configJson ?? '',
                enabled: step.enabled,
            })
            return
        }
        setForm({...defaultStepForm, sortOrder: String(nextOrder)})
        setFormError('')
    }, [nextOrder, open, step])

    const createMutation = useMutation({
        mutationFn: (payload: PipelineStepDefinitionCreateRequest) => {
            if (!pipeline) throw new Error('missing pipeline')
            return pipelineApi.createPipelineStep(pipeline.id, payload)
        },
        onSuccess: () => {
            if (pipeline) {
                qc.invalidateQueries({queryKey: ['pipeline-steps', pipeline.id]})
            }
            onOpenChange(false)
        },
        onError: (error: unknown) => {
            setFormError(
                (error as Error).message ??
                '创建步骤失败',
            )
        },
    })

    const updateMutation = useMutation({
        mutationFn: (payload: PipelineStepDefinitionUpdateRequest) => {
            if (!step) throw new Error('missing step')
            return pipelineApi.updatePipelineStep(step.id, payload)
        },
        onSuccess: () => {
            if (pipeline) {
                qc.invalidateQueries({queryKey: ['pipeline-steps', pipeline.id]})
            }
            onOpenChange(false)
        },
        onError: (error: unknown) => {
            setFormError(
                (error as Error).message ??
                '更新步骤失败',
            )
        },
    })

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault()
        setFormError('')
        const payload = {
            name: form.name.trim(),
            stepType: form.stepType,
            sortOrder: Number(form.sortOrder),
            command: emptyToUndefined(form.command),
            configJson: emptyToUndefined(form.configJson),
            enabled: form.enabled,
        }

        if (step) {
            updateMutation.mutate(payload)
            return
        }

        createMutation.mutate(payload)
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent title={step ? '编辑步骤' : '新增步骤'}
                           description="步骤顺序由 sortOrder 决定，配置 JSON 会原样传给后端。">
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="grid grid-cols-2 gap-3">
                        <Input
                            label="步骤名称"
                            value={form.name}
                            onChange={(event) => setForm((current) => ({...current, name: event.target.value}))}
                            required
                        />
                        <Select
                            label="步骤类型"
                            value={form.stepType}
                            onChange={(event) => setForm((current) => ({
                                ...current,
                                stepType: event.target.value as PipelineStepType
                            }))}
                            options={pipelineStepTypeOptions}
                        />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <Input
                            label="排序号"
                            type="number"
                            min="1"
                            value={form.sortOrder}
                            onChange={(event) => setForm((current) => ({...current, sortOrder: event.target.value}))}
                            required
                        />
                        <div
                            className="flex items-center justify-between rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 mt-6">
                            <div>
                                <p className="text-sm font-medium text-[var(--color-text)]">启用步骤</p>
                            </div>
                            <input
                                type="checkbox"
                                checked={form.enabled}
                                onChange={(event) => setForm((current) => ({
                                    ...current,
                                    enabled: event.target.checked
                                }))}
                                className="h-4 w-4 rounded border-[var(--color-border)]"
                            />
                        </div>
                    </div>
                    <div className="flex flex-col gap-1.5">
                        <label className="text-sm font-medium text-[var(--color-text)]">执行命令</label>
                        <textarea
                            value={form.command}
                            onChange={(event) => setForm((current) => ({...current, command: event.target.value}))}
                            rows={4}
                            className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] font-mono focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)]"
                            placeholder="mvn clean package -DskipTests"
                        />
                    </div>
                    <div className="flex flex-col gap-1.5">
                        <label className="text-sm font-medium text-[var(--color-text)]">扩展配置 JSON</label>
                        <textarea
                            value={form.configJson}
                            onChange={(event) => setForm((current) => ({...current, configJson: event.target.value}))}
                            rows={4}
                            className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] font-mono focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)]"
                            placeholder='{"timeoutSeconds": 600}'
                        />
                    </div>
                    {formError && (
                        <p className="text-sm text-[var(--color-danger)] bg-red-50 px-3 py-2 rounded-[var(--radius-md)]">{formError}</p>
                    )}
                    <div className="flex justify-end gap-2 pt-2">
                        <Button variant="secondary" type="button" onClick={() => onOpenChange(false)}>
                            取消
                        </Button>
                        <Button type="submit" loading={createMutation.isPending || updateMutation.isPending}>
                            {step ? '保存步骤' : '新增步骤'}
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    )
}

export function RunPipelineDialog({
                                      open,
                                      onOpenChange,
                                      pipeline,
                                      versions,
                                      onCreated,
                                  }: {
    open: boolean
    onOpenChange: (open: boolean) => void
    pipeline: PipelineDefinition | null
    versions: Version[]
    onCreated?: (runId: number) => void
}) {
    const qc = useQueryClient()
    const [form, setForm] = useState<RunFormState>(defaultRunForm)
    const [formError, setFormError] = useState('')

    useEffect(() => {
        if (open) {
            setForm(defaultRunForm)
            setFormError('')
        }
    }, [open])

    const createMutation = useMutation({
        mutationFn: (payload: PipelineRunRecordCreateRequest) => {
            if (!pipeline) throw new Error('missing pipeline')
            return pipelineApi.createPipelineRun(pipeline.id, payload)
        },
        onSuccess: (created) => {
            qc.invalidateQueries({queryKey: ['pipeline-runs']})
            onOpenChange(false)
            onCreated?.(created.id)
        },
        onError: (error: unknown) => {
            setFormError(
                (error as Error).message ??
                '创建运行失败',
            )
        },
    })

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault()
        createMutation.mutate({
            versionId: form.versionId ? Number(form.versionId) : undefined,
            branchName: emptyToUndefined(form.branchName),
            commitHash: emptyToUndefined(form.commitHash),
            imageTag: emptyToUndefined(form.imageTag),
            env: emptyToUndefined(form.env),
        })
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent title="手动运行流水线" description="运行创建后会进入等待队列，由 Runner 异步拉取执行。">
                <form onSubmit={handleSubmit} className="space-y-4">
                    <Select
                        label="关联版本"
                        value={form.versionId}
                        onChange={(event) => setForm((current) => ({...current, versionId: event.target.value}))}
                        options={[
                            {value: '', label: '不关联版本'},
                            ...versions.map((version) => ({
                                value: String(version.id),
                                label: `${version.versionNo} · ${version.title}`
                            })),
                        ]}
                    />
                    <div className="grid grid-cols-2 gap-3">
                        <Input
                            label="分支名称"
                            value={form.branchName}
                            onChange={(event) => setForm((current) => ({...current, branchName: event.target.value}))}
                            placeholder="main"
                        />
                        <Select
                            label="环境"
                            value={form.env}
                            onChange={(event) => setForm((current) => ({
                                ...current,
                                env: event.target.value as PipelineEnv | ''
                            }))}
                            options={envOptions}
                        />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <Input
                            label="Commit Hash"
                            value={form.commitHash}
                            onChange={(event) => setForm((current) => ({...current, commitHash: event.target.value}))}
                            placeholder="a1b2c3d"
                        />
                        <Input
                            label="镜像 Tag"
                            value={form.imageTag}
                            onChange={(event) => setForm((current) => ({...current, imageTag: event.target.value}))}
                            placeholder="devops/app:1.0.0"
                        />
                    </div>
                    {formError && (
                        <p className="text-sm text-[var(--color-danger)] bg-red-50 px-3 py-2 rounded-[var(--radius-md)]">{formError}</p>
                    )}
                    <div className="flex justify-end gap-2 pt-2">
                        <Button variant="secondary" type="button" onClick={() => onOpenChange(false)}>
                            取消
                        </Button>
                        <Button type="submit" loading={createMutation.isPending}>
                            创建运行
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    )
}

function RunnerStatusDialog({
                                open,
                                onOpenChange,
                                runner,
                            }: {
    open: boolean
    onOpenChange: (open: boolean) => void
    runner: RunnerRecord | null
}) {
    const qc = useQueryClient()
    const [status, setStatus] = useState<RunnerStatus>('ONLINE')

    useEffect(() => {
        if (runner && open) {
            setStatus(runner.status)
        }
    }, [open, runner])

    const mutation = useMutation({
        mutationFn: (payload: RunnerStatusUpdatePayload) => {
            if (!runner) throw new Error('missing runner')
            return pipelineApi.updateRunnerStatus(runner.id, payload)
        },
        onSuccess: () => {
            qc.invalidateQueries({queryKey: ['runners']})
            onOpenChange(false)
        },
    })

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent title="更新 Runner 状态" description="用于人工禁用节点或同步当前执行状态。">
                <div className="space-y-4">
                    <Select
                        label="状态"
                        value={status}
                        onChange={(event) => setStatus(event.target.value as RunnerStatus)}
                        options={runnerStatusOptions}
                    />
                    <div className="flex justify-end gap-2 pt-2">
                        <Button variant="secondary" type="button" onClick={() => onOpenChange(false)}>
                            取消
                        </Button>
                        <Button onClick={() => mutation.mutate({status})} loading={mutation.isPending}>
                            更新状态
                        </Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    )
}

export function PipelinesPage() {
    const qc = useQueryClient()
    const navigate = useNavigate()
    const [tab, setTab] = useState<PipelineTab>('definitions')
    const [projectFilter, setProjectFilter] = useState('')
    const [runStatusFilter, setRunStatusFilter] = useState<PipelineRunStatus | ''>('')
    const [selectedPipelineId, setSelectedPipelineId] = useState<number | null>(null)
    const [selectedRunId, setSelectedRunId] = useState<number | null>(null)
    const [pipelineDialogMode, setPipelineDialogMode] = useState<'create' | 'edit'>('create')
    const [pipelineDialogOpen, setPipelineDialogOpen] = useState(false)
    const [runDialogOpen, setRunDialogOpen] = useState(false)
    const [runnerDialogOpen, setRunnerDialogOpen] = useState(false)
    const [selectedRunner, setSelectedRunner] = useState<RunnerRecord | null>(null)

    const {data: projects = []} = useQuery({
        queryKey: ['projects'],
        queryFn: projectsApi.list,
        select: (data) => data ?? [],
    })

    const {data: repositories = []} = useQuery({
        queryKey: ['repositories'],
        queryFn: () => reposApi.list(),
        select: (data) => data ?? [],
    })

    const pipelineProjectId = projectFilter ? Number(projectFilter) : undefined
    const {data: pipelines = [], isLoading: isPipelinesLoading} = useQuery({
        queryKey: ['pipelines', pipelineProjectId],
        queryFn: () => pipelineApi.listPipelines(pipelineProjectId),
        select: (data) => data ?? [],
    })

    useEffect(() => {
        if (!pipelines.length) {
            setSelectedPipelineId(null)
            return
        }
        if (!selectedPipelineId || !pipelines.some((pipeline) => pipeline.id === selectedPipelineId)) {
            setSelectedPipelineId(pipelines[0].id)
        }
    }, [pipelines, selectedPipelineId])

    const selectedPipeline = useMemo(
        () => pipelines.find((pipeline) => pipeline.id === selectedPipelineId) ?? null,
        [pipelines, selectedPipelineId],
    )

    const {data: selectedPipelineSteps = [], isLoading: isStepsLoading} = useQuery({
        queryKey: ['pipeline-steps', selectedPipelineId],
        queryFn: () => pipelineApi.listPipelineSteps(selectedPipelineId as number),
        enabled: Boolean(selectedPipelineId),
        select: (data) => (data ?? []).slice().sort((left, right) => left.sortOrder - right.sortOrder),
    })

    const runQuery = useMemo<PipelineRunRecordQueryRequest>(
        () => ({
            projectId: pipelineProjectId,
            pipelineId: tab === 'runs' && selectedPipelineId ? selectedPipelineId : undefined,
            status: runStatusFilter || undefined,
        }),
        [pipelineProjectId, runStatusFilter, selectedPipelineId, tab],
    )

    const {data: runs = [], isLoading: isRunsLoading} = useQuery({
        queryKey: ['pipeline-runs', runQuery],
        queryFn: () => pipelineApi.listPipelineRuns(runQuery),
        select: (data) => (data ?? []).slice().sort((left, right) => +new Date(right.createdAt) - +new Date(left.createdAt)),
    })

    useEffect(() => {
        if (!runs.length) {
            setSelectedRunId(null)
            return
        }
        if (!selectedRunId || !runs.some((run) => run.id === selectedRunId)) {
            setSelectedRunId(runs[0].id)
        }
    }, [runs, selectedRunId])

    const selectedRun = useMemo(
        () => runs.find((run) => run.id === selectedRunId) ?? null,
        [runs, selectedRunId],
    )

    const {data: runners = [], isLoading: isRunnersLoading} = useQuery({
        queryKey: ['runners'],
        queryFn: pipelineApi.listRunners,
        select: (data) => data ?? [],
    })

    const selectedPipelineVersionsProjectId = selectedPipeline?.projectId
    const {data: versions = []} = useQuery({
        queryKey: ['versions-for-pipeline', selectedPipelineVersionsProjectId],
        queryFn: () => releasesApi.list(selectedPipelineVersionsProjectId),
        enabled: Boolean(selectedPipelineVersionsProjectId),
        select: (data) => data ?? [],
    })

    const removePipelineMutation = useMutation({
        mutationFn: (id: number) => pipelineApi.removePipeline(id),
        onSuccess: () => {
            qc.invalidateQueries({queryKey: ['pipelines']})
            qc.invalidateQueries({queryKey: ['pipeline-runs']})
        },
    })

    const runStats = useMemo(() => {
        return runs.reduce(
            (accumulator, run) => {
                accumulator.total += 1
                accumulator[run.status] += 1
                return accumulator
            },
            {
                total: 0,
                PENDING: 0,
                RUNNING: 0,
                SUCCESS: 0,
                FAILED: 0,
                CANCELLED: 0
            } as Record<'total' | PipelineRunStatus, number>,
        )
    }, [runs])

    const projectMap = useMemo(() => new Map(projects.map((project) => [project.id, project.name])), [projects])
    const repoMap = useMemo(() => new Map(repositories.map((repo) => [repo.id, repo.repoName])), [repositories])

    const recentRuns = useMemo(
        () => runs.filter((run) => run.pipelineId === selectedPipelineId).slice(0, 5),
        [runs, selectedPipelineId],
    )

    return (
        <div className="p-8 max-w-7xl mx-auto space-y-6">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
                <div>
                    <h1 className="text-2xl font-semibold text-[var(--color-text)]">Pipelines</h1>
                    <p className="text-sm text-[var(--color-text-muted)] mt-1">
                        管理流水线定义、执行记录与 Runner 资源，形成独立的流水线中心。
                    </p>
                </div>
                <div className="flex flex-wrap items-center gap-3">
                    <Select
                        value={projectFilter}
                        onChange={(event) => setProjectFilter(event.target.value)}
                        options={[
                            {value: '', label: '全部项目'},
                            ...projects.map((project) => ({value: String(project.id), label: project.name})),
                        ]}
                        className="min-w-44"
                    />
                    <Button
                        variant="secondary"
                        onClick={() => {
                            qc.invalidateQueries({queryKey: ['pipelines']})
                            qc.invalidateQueries({queryKey: ['pipeline-runs']})
                            qc.invalidateQueries({queryKey: ['runners']})
                        }}
                    >
                        <RefreshCw size={15}/>
                        刷新数据
                    </Button>
                    <Button
                        onClick={() => {
                            setPipelineDialogMode('create')
                            setPipelineDialogOpen(true)
                        }}
                    >
                        <Plus size={15}/>
                        新建流水线
                    </Button>
                </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                <SectionCard className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-[var(--color-text-muted)]">流水线总数</p>
                            <p className="text-3xl font-semibold text-[var(--color-text)] mt-2">{pipelines.length}</p>
                        </div>
                        <div
                            className="w-11 h-11 rounded-full bg-[var(--color-primary-light)] text-[var(--color-primary)] flex items-center justify-center">
                            <Workflow size={20}/>
                        </div>
                    </div>
                </SectionCard>
                <SectionCard className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-[var(--color-text-muted)]">运行中任务</p>
                            <p className="text-3xl font-semibold text-[var(--color-text)] mt-2">{runStats.RUNNING}</p>
                        </div>
                        <div
                            className="w-11 h-11 rounded-full bg-blue-50 text-blue-700 flex items-center justify-center">
                            <Activity size={20}/>
                        </div>
                    </div>
                </SectionCard>
                <SectionCard className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-[var(--color-text-muted)]">成功运行</p>
                            <p className="text-3xl font-semibold text-[var(--color-text)] mt-2">{runStats.SUCCESS}</p>
                        </div>
                        <div
                            className="w-11 h-11 rounded-full bg-emerald-50 text-emerald-700 flex items-center justify-center">
                            <Rocket size={20}/>
                        </div>
                    </div>
                </SectionCard>
                <SectionCard className="p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-[var(--color-text-muted)]">在线 Runner</p>
                            <p className="text-3xl font-semibold text-[var(--color-text)] mt-2">
                                {runners.filter((runner) => runner.status === 'ONLINE' || runner.status === 'BUSY').length}
                            </p>
                        </div>
                        <div
                            className="w-11 h-11 rounded-full bg-amber-50 text-amber-700 flex items-center justify-center">
                            <Server size={20}/>
                        </div>
                    </div>
                </SectionCard>
            </div>

            <div className="flex flex-wrap gap-2">
                {[
                    {key: 'definitions', label: '流水线定义', icon: Workflow},
                    {key: 'runs', label: '运行记录', icon: Terminal},
                    {key: 'runners', label: 'Runner 管理', icon: Server},
                ].map((item) => {
                    const Icon = item.icon
                    const isActive = tab === item.key
                    return (
                        <button
                            key={item.key}
                            type="button"
                            onClick={() => setTab(item.key as PipelineTab)}
                            className={`inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm transition-colors ${
                                isActive
                                    ? 'bg-[var(--color-primary)] text-white'
                                    : 'bg-[var(--color-surface)] border border-[var(--color-border)] text-[var(--color-text-muted)] hover:text-[var(--color-text)]'
                            }`}
                        >
                            <Icon size={15}/>
                            {item.label}
                        </button>
                    )
                })}
            </div>

            {tab === 'definitions' && (
                <div className="grid gap-6 xl:grid-cols-[360px_minmax(0,1fr)]">
                    <SectionCard className="overflow-hidden">
                        <div className="p-5 border-b border-[var(--color-border)] flex items-center justify-between">
                            <div>
                                <h2 className="text-lg font-semibold text-[var(--color-text)]">流水线清单</h2>
                                <p className="text-sm text-[var(--color-text-muted)] mt-1">按项目筛选并选择目标流水线。</p>
                            </div>
                        </div>
                        <div className="divide-y divide-[var(--color-border)] max-h-[720px] overflow-y-auto">
                            {isPipelinesLoading ? (
                                Array.from({length: 4}).map((_, index) => (
                                    <div key={index} className="p-5 animate-pulse space-y-3">
                                        <div className="h-4 bg-[var(--color-surface-3)] rounded w-2/3"/>
                                        <div className="h-3 bg-[var(--color-surface-3)] rounded w-1/2"/>
                                    </div>
                                ))
                            ) : pipelines.length === 0 ? (
                                <div className="p-10 text-center text-[var(--color-text-muted)]">
                                    <Workflow size={28} className="mx-auto mb-3 text-[var(--color-border)]"/>
                                    当前筛选下还没有流水线。
                                </div>
                            ) : (
                                pipelines.map((pipeline) => (
                                    <button
                                        key={pipeline.id}
                                        type="button"
                                        onClick={() => setSelectedPipelineId(pipeline.id)}
                                        className={`w-full p-5 text-left transition-colors ${
                                            selectedPipelineId === pipeline.id
                                                ? 'bg-[var(--color-primary-light)]'
                                                : 'hover:bg-[var(--color-surface-2)]'
                                        }`}
                                    >
                                        <div className="flex items-start justify-between gap-3">
                                            <div className="min-w-0">
                                                <div className="flex items-center gap-2">
                                                    <p className="text-sm font-semibold text-[var(--color-text)] truncate">{pipeline.name}</p>
                                                    <Badge variant={pipeline.enabled ? 'success' : 'warning'}>
                                                        {pipeline.enabled ? '启用' : '停用'}
                                                    </Badge>
                                                </div>
                                                <p className="text-xs text-[var(--color-text-subtle)] font-mono mt-1">{pipeline.code}</p>
                                                <div className="flex items-center gap-2 mt-2 flex-wrap">
                          <span
                              className="text-xs text-[var(--color-text-muted)] bg-[var(--color-surface-3)] px-2 py-1 rounded-full">
                            {projectMap.get(pipeline.projectId) ?? `项目 #${pipeline.projectId}`}
                          </span>
                                                    <span
                                                        className="text-xs text-[var(--color-text-muted)] bg-[var(--color-surface-3)] px-2 py-1 rounded-full">
                            {repoMap.get(pipeline.repositoryId) ?? `仓库 #${pipeline.repositoryId}`}
                          </span>
                                                </div>
                                            </div>
                                        </div>
                                        <p className="text-xs text-[var(--color-text-muted)] mt-3">更新于 {formatDateTime(pipeline.updatedAt)}</p>
                                        <div className="mt-3 flex justify-end">
                      <span className="inline-flex items-center gap-1 text-xs text-[var(--color-primary)]">
                        打开详情
                        <ExternalLink size={12}/>
                      </span>
                                        </div>
                                    </button>
                                ))
                            )}
                        </div>
                    </SectionCard>

                    <div className="space-y-6 min-w-0">
                        <SectionCard className="p-6">
                            {selectedPipeline ? (
                                <>
                                    <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                                        <div className="space-y-3 min-w-0">
                                            <div className="flex items-center gap-2 flex-wrap">
                                                <h2 className="text-xl font-semibold text-[var(--color-text)]">{selectedPipeline.name}</h2>
                                                <Badge variant={selectedPipeline.enabled ? 'success' : 'warning'}>
                                                    {selectedPipeline.enabled ? '启用中' : '已停用'}
                                                </Badge>
                                                <Badge variant="muted">{selectedPipeline.triggerTypeDescription}</Badge>
                                            </div>
                                            <p className="text-sm text-[var(--color-text-muted)]">{selectedPipeline.description || '暂无描述'}</p>
                                            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
                                                <div>
                                                    <FieldLabel>编码</FieldLabel>
                                                    <p className="text-sm font-mono text-[var(--color-text)] mt-1">{selectedPipeline.code}</p>
                                                </div>
                                                <div>
                                                    <FieldLabel>项目</FieldLabel>
                                                    <p className="text-sm text-[var(--color-text)] mt-1">
                                                        {projectMap.get(selectedPipeline.projectId) ?? `项目 #${selectedPipeline.projectId}`}
                                                    </p>
                                                </div>
                                                <div>
                                                    <FieldLabel>仓库</FieldLabel>
                                                    <p className="text-sm text-[var(--color-text)] mt-1">
                                                        {repoMap.get(selectedPipeline.repositoryId) ?? `仓库 #${selectedPipeline.repositoryId}`}
                                                    </p>
                                                </div>
                                                <div>
                                                    <FieldLabel>更新时间</FieldLabel>
                                                    <p className="text-sm text-[var(--color-text)] mt-1">{formatDateTime(selectedPipeline.updatedAt)}</p>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="flex flex-wrap gap-2">
                                            <Button
                                                variant="secondary"
                                                onClick={() => navigate(`/pipelines/${selectedPipeline.id}`)}
                                            >
                                                <ExternalLink size={15}/>
                                                进入详情页
                                            </Button>
                                            <Button variant="secondary" onClick={() => setRunDialogOpen(true)}>
                                                <Play size={15}/>
                                                运行
                                            </Button>
                                            <Button
                                                variant="danger"
                                                onClick={() => {
                                                    if (confirm(`删除流水线“${selectedPipeline.name}”？`)) {
                                                        removePipelineMutation.mutate(selectedPipeline.id)
                                                    }
                                                }}
                                            >
                                                <Trash2 size={15}/>
                                                删除
                                            </Button>
                                        </div>
                                    </div>
                                </>
                            ) : (
                                <div
                                    className="py-12 text-center text-[var(--color-text-muted)]">选择左侧流水线以查看详情。</div>
                            )}
                        </SectionCard>

                        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
                            <SectionCard className="overflow-hidden">
                                <div
                                    className="p-5 border-b border-[var(--color-border)] flex items-center justify-between">
                                    <div>
                                        <h3 className="text-lg font-semibold text-[var(--color-text)]">编排概览</h3>
                                        <p className="text-sm text-[var(--color-text-muted)] mt-1">详情页中可编辑步骤、配置执行命令和调整顺序。</p>
                                    </div>
                                    <Button
                                        size="sm"
                                        onClick={() => selectedPipeline && navigate(`/pipelines/${selectedPipeline.id}`)}
                                        disabled={!selectedPipeline}
                                    >
                                        <ExternalLink size={14}/>
                                        打开详情
                                    </Button>
                                </div>
                                <div className="divide-y divide-[var(--color-border)]">
                                    {isStepsLoading ? (
                                        Array.from({length: 3}).map((_, index) => (
                                            <div key={index} className="p-5 animate-pulse space-y-3">
                                                <div className="h-4 bg-[var(--color-surface-3)] rounded w-1/3"/>
                                                <div className="h-3 bg-[var(--color-surface-3)] rounded w-2/3"/>
                                            </div>
                                        ))
                                    ) : !selectedPipeline ? (
                                        <div
                                            className="p-10 text-center text-[var(--color-text-muted)]">请选择流水线后查看概览。</div>
                                    ) : selectedPipelineSteps.length === 0 ? (
                                        <div
                                            className="p-10 text-center text-[var(--color-text-muted)]">当前流水线还没有步骤。</div>
                                    ) : (
                                        selectedPipelineSteps.slice(0, 3).map((step) => (
                                            <div key={step.id}
                                                 className="p-5 flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                                                <div className="min-w-0">
                                                    <div className="flex items-center gap-2 flex-wrap">
                                                        <Badge variant="muted">#{step.sortOrder}</Badge>
                                                        <p className="text-sm font-semibold text-[var(--color-text)]">{step.name}</p>
                                                        <Badge variant="info">{step.stepTypeDescription}</Badge>
                                                        <Badge
                                                            variant={step.enabled ? 'success' : 'warning'}>{step.enabled ? '启用' : '停用'}</Badge>
                                                    </div>
                                                    {step.command && (
                                                        <pre
                                                            className="mt-3 text-xs text-[var(--color-text-muted)] bg-[var(--color-surface-2)] rounded-[var(--radius-md)] p-3 overflow-x-auto">
                              {step.command}
                            </pre>
                                                    )}
                                                    {step.configJson && (
                                                        <p className="text-xs text-[var(--color-text-subtle)] mt-2">配置
                                                            JSON: {step.configJson}</p>
                                                    )}
                                                </div>
                                                <div className="text-xs text-[var(--color-text-subtle)]">顺序
                                                    #{step.sortOrder}</div>
                                            </div>
                                        ))
                                    )}
                                    {selectedPipeline && selectedPipelineSteps.length > 3 && (
                                        <div className="p-4 text-center text-sm text-[var(--color-text-muted)]">
                                            还有 {selectedPipelineSteps.length - 3} 个步骤，进入详情页查看全部。
                                        </div>
                                    )}
                                </div>
                            </SectionCard>

                            <SectionCard className="p-5">
                                <div className="flex items-center justify-between">
                                    <div>
                                        <h3 className="text-lg font-semibold text-[var(--color-text)]">最近运行</h3>
                                        <p className="text-sm text-[var(--color-text-muted)] mt-1">选中流水线的最近 5
                                            次运行。</p>
                                    </div>
                                    <button
                                        type="button"
                                        onClick={() => setTab('runs')}
                                        className="text-sm text-[var(--color-primary)] hover:underline"
                                    >
                                        查看全部
                                    </button>
                                </div>
                                <div className="mt-4 space-y-3">
                                    {recentRuns.length === 0 ? (
                                        <div className="text-sm text-[var(--color-text-muted)]">暂无运行记录。</div>
                                    ) : (
                                        recentRuns.map((run) => (
                                            <button
                                                key={run.id}
                                                type="button"
                                                onClick={() => navigate(`/pipelines/runs/${run.id}`)}
                                                className="w-full text-left rounded-[var(--radius-lg)] border border-[var(--color-border)] p-3 hover:bg-[var(--color-surface-2)] transition-colors"
                                            >
                                                <div className="flex items-center justify-between gap-3">
                                                    <div>
                                                        <p className="text-sm font-medium text-[var(--color-text)]">{run.runNo}</p>
                                                        <p className="text-xs text-[var(--color-text-subtle)] mt-1">{formatDateTime(run.createdAt)}</p>
                                                    </div>
                                                    <Badge
                                                        variant={pipelineStatusTone[run.status]}>{run.statusDescription}</Badge>
                                                </div>
                                            </button>
                                        ))
                                    )}
                                </div>
                            </SectionCard>
                        </div>
                    </div>
                </div>
            )}

            {tab === 'runs' && (
                <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
                    <SectionCard className="overflow-hidden min-w-0">
                        <div
                            className="p-5 border-b border-[var(--color-border)] flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
                            <div>
                                <h2 className="text-lg font-semibold text-[var(--color-text)]">运行记录</h2>
                                <p className="text-sm text-[var(--color-text-muted)] mt-1">查看排队、执行、完成和失败的流水线任务。</p>
                            </div>
                            <div className="flex flex-wrap items-center gap-3">
                                <Select
                                    value={selectedPipelineId ? String(selectedPipelineId) : ''}
                                    onChange={(event) => setSelectedPipelineId(event.target.value ? Number(event.target.value) : null)}
                                    options={[
                                        {value: '', label: '全部流水线'},
                                        ...pipelines.map((pipeline) => ({
                                            value: String(pipeline.id),
                                            label: pipeline.name
                                        })),
                                    ]}
                                    className="min-w-44"
                                />
                                <Select
                                    value={runStatusFilter}
                                    onChange={(event) => setRunStatusFilter(event.target.value as PipelineRunStatus | '')}
                                    options={[
                                        {value: '', label: '全部状态'},
                                        {value: 'PENDING', label: '等待中'},
                                        {value: 'RUNNING', label: '运行中'},
                                        {value: 'SUCCESS', label: '成功'},
                                        {value: 'FAILED', label: '失败'},
                                        {value: 'CANCELLED', label: '已取消'},
                                    ]}
                                    className="min-w-32"
                                />
                                <Button onClick={() => setRunDialogOpen(true)} disabled={!selectedPipeline}>
                                    <Play size={14}/>
                                    运行选中流水线
                                </Button>
                            </div>
                        </div>
                        <div className="divide-y divide-[var(--color-border)] max-h-[820px] overflow-y-auto">
                            {isRunsLoading ? (
                                Array.from({length: 5}).map((_, index) => (
                                    <div key={index} className="p-5 animate-pulse space-y-3">
                                        <div className="h-4 bg-[var(--color-surface-3)] rounded w-1/3"/>
                                        <div className="h-3 bg-[var(--color-surface-3)] rounded w-2/3"/>
                                    </div>
                                ))
                            ) : runs.length === 0 ? (
                                <div
                                    className="p-10 text-center text-[var(--color-text-muted)]">当前筛选下没有运行记录。</div>
                            ) : (
                                runs.map((run) => (
                                    <button
                                        key={run.id}
                                        type="button"
                                        onClick={() => setSelectedRunId(run.id)}
                                        className={`w-full p-5 text-left transition-colors ${
                                            selectedRunId === run.id ? 'bg-[var(--color-primary-light)]' : 'hover:bg-[var(--color-surface-2)]'
                                        }`}
                                    >
                                        <div
                                            className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                                            <div className="min-w-0">
                                                <div className="flex items-center gap-2 flex-wrap">
                                                    <p className="text-sm font-semibold text-[var(--color-text)]">{run.runNo}</p>
                                                    <Badge
                                                        variant={pipelineStatusTone[run.status]}>{run.statusDescription}</Badge>
                                                    <Badge variant="muted">{run.triggerTypeDescription}</Badge>
                                                </div>
                                                <div
                                                    className="mt-2 flex flex-wrap items-center gap-2 text-xs text-[var(--color-text-muted)]">
                                                    <span>{pipelines.find((pipeline) => pipeline.id === run.pipelineId)?.name ?? `流水线 #${run.pipelineId}`}</span>
                                                    <span>·</span>
                                                    <span>{run.branchName || '未指定分支'}</span>
                                                    <span>·</span>
                                                    <span>{run.imageTag || '未指定镜像'}</span>
                                                    <span>·</span>
                                                    <span>{run.env || '未指定环境'}</span>
                                                </div>
                                            </div>
                                            <div className="text-xs text-[var(--color-text-subtle)] lg:text-right">
                                                <div>创建于 {formatDateTime(run.createdAt)}</div>
                                                <div>耗时 {formatDuration(run.durationSeconds)}</div>
                                            </div>
                                        </div>
                                    </button>
                                ))
                            )}
                        </div>
                    </SectionCard>

                    <SectionCard className="overflow-hidden min-w-0">
                        <div className="p-5 border-b border-[var(--color-border)]">
                            <h3 className="text-lg font-semibold text-[var(--color-text)]">详情入口</h3>
                            <p className="text-sm text-[var(--color-text-muted)] mt-1">运行明细已拆分为独立页面。</p>
                        </div>
                        {selectedRun ? (
                            <div className="p-5 space-y-5">
                                <div className="space-y-3">
                                    <div className="flex items-center gap-2 flex-wrap">
                                        <p className="text-base font-semibold text-[var(--color-text)]">{selectedRun.runNo}</p>
                                        <Badge
                                            variant={pipelineStatusTone[selectedRun.status]}>{selectedRun.statusDescription}</Badge>
                                    </div>
                                    <div className="grid grid-cols-2 gap-3 text-sm">
                                        <div>
                                            <FieldLabel>流水线</FieldLabel>
                                            <p className="mt-1 text-[var(--color-text)]">
                                                {pipelines.find((pipeline) => pipeline.id === selectedRun.pipelineId)?.name ?? `#${selectedRun.pipelineId}`}
                                            </p>
                                        </div>
                                        <div>
                                            <FieldLabel>环境</FieldLabel>
                                            <p className="mt-1 text-[var(--color-text)]">{selectedRun.env || '—'}</p>
                                        </div>
                                        <div>
                                            <FieldLabel>开始时间</FieldLabel>
                                            <p className="mt-1 text-[var(--color-text)]">{formatDateTime(selectedRun.startedAt)}</p>
                                        </div>
                                        <div>
                                            <FieldLabel>耗时</FieldLabel>
                                            <p className="mt-1 text-[var(--color-text)]">{formatDuration(selectedRun.durationSeconds)}</p>
                                        </div>
                                    </div>
                                </div>
                                <Button onClick={() => navigate(`/pipelines/runs/${selectedRun.id}`)}>
                                    <ExternalLink size={15}/>
                                    打开运行详情页
                                </Button>
                            </div>
                        ) : (
                            <div
                                className="p-10 text-center text-[var(--color-text-muted)]">选择左侧运行记录后可进入独立详情页。</div>
                        )}
                    </SectionCard>
                </div>
            )}

            {tab === 'runners' && (
                <SectionCard className="overflow-hidden">
                    <div
                        className="p-5 border-b border-[var(--color-border)] flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                        <div>
                            <h2 className="text-lg font-semibold text-[var(--color-text)]">Runner 管理</h2>
                            <p className="text-sm text-[var(--color-text-muted)] mt-1">查看执行节点负载与心跳，支持人工切换状态。</p>
                        </div>
                        <Button variant="secondary" onClick={() => qc.invalidateQueries({queryKey: ['runners']})}>
                            <RefreshCw size={15}/>
                            刷新 Runner
                        </Button>
                    </div>
                    <div className="divide-y divide-[var(--color-border)]">
                        {isRunnersLoading ? (
                            Array.from({length: 4}).map((_, index) => (
                                <div key={index} className="p-5 animate-pulse space-y-3">
                                    <div className="h-4 bg-[var(--color-surface-3)] rounded w-1/4"/>
                                    <div className="h-3 bg-[var(--color-surface-3)] rounded w-1/2"/>
                                </div>
                            ))
                        ) : runners.length === 0 ? (
                            <div className="p-10 text-center text-[var(--color-text-muted)]">
                                <CircleOff size={28} className="mx-auto mb-3 text-[var(--color-border)]"/>
                                当前还没有注册的 Runner。
                            </div>
                        ) : (
                            runners.map((runner) => (
                                <div key={runner.id}
                                     className="p-5 flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
                                    <div className="min-w-0">
                                        <div className="flex items-center gap-2 flex-wrap">
                                            <p className="text-sm font-semibold text-[var(--color-text)]">{runner.runnerName}</p>
                                            <Badge
                                                variant={runnerStatusTone[runner.status]}>{runner.statusDescription}</Badge>
                                        </div>
                                        <div
                                            className="mt-2 flex flex-wrap items-center gap-2 text-xs text-[var(--color-text-muted)]">
                                            <span>{runner.ip}:{runner.port}</span>
                                            <span>·</span>
                                            <span>并发 {runner.currentConcurrency}/{runner.maxConcurrency}</span>
                                            <span>·</span>
                                            <span>最后心跳 {formatDateTime(runner.lastHeartbeatAt)}</span>
                                        </div>
                                    </div>
                                    <div className="grid gap-3 sm:grid-cols-3 xl:min-w-[380px]">
                                        <div
                                            className="rounded-[var(--radius-lg)] bg-[var(--color-surface-2)] px-4 py-3">
                                            <p className="text-xs text-[var(--color-text-subtle)]">已注册</p>
                                            <p className="text-sm text-[var(--color-text)] mt-1">{formatDateTime(runner.registeredAt)}</p>
                                        </div>
                                        <div
                                            className="rounded-[var(--radius-lg)] bg-[var(--color-surface-2)] px-4 py-3">
                                            <p className="text-xs text-[var(--color-text-subtle)]">更新时间</p>
                                            <p className="text-sm text-[var(--color-text)] mt-1">{formatDateTime(runner.updatedAt)}</p>
                                        </div>
                                        <div className="flex items-center justify-end">
                                            <Button
                                                variant="secondary"
                                                onClick={() => {
                                                    setSelectedRunner(runner)
                                                    setRunnerDialogOpen(true)
                                                }}
                                            >
                                                <Settings2 size={15}/>
                                                更新状态
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </SectionCard>
            )}

            <PipelineFormDialog
                open={pipelineDialogOpen}
                onOpenChange={setPipelineDialogOpen}
                mode={pipelineDialogMode}
                pipeline={pipelineDialogMode === 'edit' ? selectedPipeline : null}
                projects={projects}
                repositories={repositories}
                onCreated={(pipeline) => navigate(`/pipelines/${pipeline.id}`)}
            />
            <RunPipelineDialog
                open={runDialogOpen}
                onOpenChange={setRunDialogOpen}
                pipeline={selectedPipeline}
                versions={versions}
                onCreated={(runId) => navigate(`/pipelines/runs/${runId}`)}
            />
            <RunnerStatusDialog open={runnerDialogOpen} onOpenChange={setRunnerDialogOpen} runner={selectedRunner}/>
        </div>
    )
}