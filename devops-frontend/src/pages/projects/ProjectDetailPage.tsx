import { useState, type Dispatch, type SetStateAction } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import * as Tabs from '@radix-ui/react-tabs'
import { ArrowLeft, Trash2, Plus, Flag, KeyRound } from 'lucide-react'
import { projectModulesApi, projectsApi, taskCommentsApi, taskPropertiesApi, taskPropertyValuesApi, tasksApi } from '@/api/work'
import { reposApi } from '@/api/code'
import { pipelineApi } from '@/api/pipeline'
import { Button } from '@/components/ui/Button'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/Dialog'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import type {
  Credential,
  CredentialCreateRequest,
  CredentialUpdateRequest,
  ProjectModuleTree,
  Task,
  TaskCommentTree,
  TaskCreateRequest,
  TaskDetail,
  TaskPriority,
  TaskProperty,
  TaskPropertyCreateRequest,
  TaskPropertyType,
  TaskPropertyValue,
  TaskStatus,
  TaskUpdateRequest,
  TaskType,
} from '@/types'

const PRIORITY_STYLES: Record<TaskPriority, string> = {
  LOW: 'bg-gray-100 text-gray-600',
  MEDIUM: 'bg-blue-50 text-blue-700',
  HIGH: 'bg-amber-50 text-amber-700',
  URGENT: 'bg-red-50 text-red-600',
}

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: 'TODO', label: 'To Do' },
  { status: 'IN_PROGRESS', label: 'In Progress' },
  { status: 'TESTING', label: 'Testing' },
  { status: 'DONE', label: 'Done' },
]

const TASK_TYPE_OPTIONS: { value: TaskType; label: string }[] = [
  { value: 'TASK', label: '任务' },
  { value: 'REQUIREMENT', label: '需求' },
  { value: 'BUG', label: '缺陷' },
  { value: 'STORY', label: '用户故事' },
  { value: 'SUB_TASK', label: '子任务' },
]

const TASK_PROPERTY_TYPE_OPTIONS: { value: TaskPropertyType; label: string }[] = [
  { value: 'TEXT', label: '文本' },
  { value: 'NUMBER', label: '数字' },
  { value: 'DATE', label: '日期时间' },
  { value: 'SELECT', label: '单选' },
  { value: 'MULTI_SELECT', label: '多选' },
  { value: 'USER', label: '用户 ID' },
  { value: 'BOOLEAN', label: '布尔值' },
]

const TASK_STATUS_OPTIONS: { value: TaskStatus; label: string }[] = [
  { value: 'TODO', label: '待开始' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'TESTING', label: '测试中' },
  { value: 'DONE', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
]

const TASK_PRIORITY_OPTIONS: { value: TaskPriority; label: string }[] = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'URGENT', label: '紧急' },
]

type TaskPropertyFormState = Record<number, string | string[]>
type TaskCreateFormState = Omit<TaskCreateRequest, 'projectId'>
type ModuleOption = { value: string; label: string }

function formatDateTime(value?: string | null) {
  if (!value) return '未设置'
  return value.replace('T', ' ').slice(0, 16)
}

function toDateTimeInputValue(value?: string | null) {
  if (!value) return ''
  return value.slice(0, 16)
}

function parseOptionalNumber(value: string) {
  if (!value.trim()) return undefined
  const parsed = Number(value)
  return Number.isNaN(parsed) ? undefined : parsed
}

function parseOptions(optionsJson?: string | null) {
  if (!optionsJson) return []
  try {
    const parsed = JSON.parse(optionsJson) as Array<string | { label?: string; value?: string }>
    if (!Array.isArray(parsed)) return []
    return parsed
      .map((item) => {
        if (typeof item === 'string') {
          return { label: item, value: item }
        }
        return {
          label: item.label ?? item.value ?? '',
          value: item.value ?? item.label ?? '',
        }
      })
      .filter((item) => item.value)
  } catch {
    return []
  }
}

function parseMultiSelectValue(valueText?: string | null) {
  if (!valueText) return []
  try {
    const parsed = JSON.parse(valueText) as string[]
    return Array.isArray(parsed) ? parsed.filter((item) => typeof item === 'string') : []
  } catch {
    return []
  }
}

function buildTaskPropertyForm(detail?: TaskDetail): TaskPropertyFormState {
  if (!detail) return {}
  return detail.propertyValues.reduce<TaskPropertyFormState>((acc, item) => {
    acc[item.propertyId] =
      item.propertyType === 'MULTI_SELECT'
        ? parseMultiSelectValue(item.valueText)
        : (item.valueText ?? '')
    return acc
  }, {})
}

function createEmptyTaskForm(): TaskCreateFormState {
  return {
    title: '',
    taskType: 'TASK',
    description: '',
    priority: 'MEDIUM',
    sortOrder: 0,
  }
}

function buildTaskUpdateForm(detail: TaskDetail): TaskUpdateRequest {
  return {
    projectId: detail.projectId,
    moduleId: detail.moduleId ?? undefined,
    parentTaskId: detail.parentTaskId ?? undefined,
    title: detail.title,
    taskType: detail.taskType,
    description: detail.description || '',
    assigneeId: detail.assigneeId ?? undefined,
    reporterId: detail.reporterId ?? undefined,
    status: detail.status,
    priority: detail.priority,
    dueDate: detail.dueDate ?? undefined,
    estimatedHours: detail.estimatedHours ?? undefined,
    actualHours: detail.actualHours ?? undefined,
    sortOrder: detail.sortOrder ?? 0,
  }
}

function flattenModuleOptions(modules: ProjectModuleTree[], depth = 0): ModuleOption[] {
  return modules.flatMap((module) => [
    {
      value: String(module.id),
      label: `${depth > 0 ? `${'　'.repeat(depth)}└ ` : ''}${module.name}`,
    },
    ...flattenModuleOptions(module.children ?? [], depth + 1),
  ])
}

function renderTaskPropertyInput(
  property: TaskPropertyValue,
  value: string | string[],
  onChange: (nextValue: string | string[]) => void,
) {
  const options = parseOptions(property.optionsJson)

  if (property.propertyType === 'SELECT') {
    return (
      <select
        value={typeof value === 'string' ? value : ''}
        onChange={(e) => onChange(e.target.value)}
        className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)]"
      >
        <option value="">请选择</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    )
  }

  if (property.propertyType === 'MULTI_SELECT') {
    const selectedValues = Array.isArray(value) ? value : []
    return (
      <div className="flex flex-wrap gap-3 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2">
        {options.length === 0 && (
          <span className="text-xs text-[var(--color-text-subtle)]">该属性未配置选项</span>
        )}
        {options.map((option) => {
          const checked = selectedValues.includes(option.value)
          return (
            <label key={option.value} className="inline-flex items-center gap-2 text-sm text-[var(--color-text)]">
              <input
                type="checkbox"
                checked={checked}
                onChange={(e) => {
                  const nextValue = e.target.checked
                    ? [...selectedValues, option.value]
                    : selectedValues.filter((item) => item !== option.value)
                  onChange(nextValue)
                }}
              />
              {option.label}
            </label>
          )
        })}
      </div>
    )
  }

  if (property.propertyType === 'BOOLEAN') {
    return (
      <label className="inline-flex items-center gap-2 text-sm text-[var(--color-text)]">
        <input
          type="checkbox"
          checked={value === 'true'}
          onChange={(e) => onChange(e.target.checked ? 'true' : 'false')}
        />
        {value === '' ? '未设置' : value === 'true' ? '是' : '否'}
      </label>
    )
  }

  if (property.propertyType === 'DATE') {
    return (
      <input
        type="datetime-local"
        value={typeof value === 'string' ? toDateTimeInputValue(value) : ''}
        onChange={(e) => onChange(e.target.value)}
        className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)]"
      />
    )
  }

  return (
    <input
      type={property.propertyType === 'NUMBER' ? 'number' : 'text'}
      value={typeof value === 'string' ? value : ''}
      onChange={(e) => onChange(e.target.value)}
      placeholder={property.propertyType === 'USER' ? '请输入用户 ID' : ''}
      className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)]"
    />
  )
}

function TaskCommentItem({
  comment,
  onReply,
  onDelete,
}: {
  comment: TaskCommentTree
  onReply: (parentId: number, content: string) => void
  onDelete: (id: number) => void
}) {
  const [replying, setReplying] = useState(false)
  const [replyContent, setReplyContent] = useState('')

  return (
    <div className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-3">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm text-[var(--color-text)] whitespace-pre-wrap">{comment.content}</p>
          <p className="text-xs text-[var(--color-text-subtle)] mt-1">
            用户 {comment.createdBy ?? '-'} · {formatDateTime(comment.createdAt)}
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <button
            type="button"
            className="text-xs text-[var(--color-primary)] hover:underline"
            onClick={() => setReplying((open) => !open)}
          >
            回复
          </button>
          <button
            type="button"
            className="text-xs text-red-500 hover:underline"
            onClick={() => onDelete(comment.id)}
          >
            删除
          </button>
        </div>
      </div>

      {replying && (
        <form
          className="mt-3 space-y-2"
          onSubmit={(e) => {
            e.preventDefault()
            const content = replyContent.trim()
            if (!content) return
            onReply(comment.id, content)
            setReplyContent('')
            setReplying(false)
          }}
        >
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            rows={3}
            placeholder="输入回复内容"
            className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
          />
          <div className="flex justify-end gap-2">
            <Button size="sm" variant="secondary" type="button" onClick={() => setReplying(false)}>
              取消
            </Button>
            <Button size="sm" type="submit">
              提交回复
            </Button>
          </div>
        </form>
      )}

      {comment.children.length > 0 && (
        <div className="mt-3 ml-4 space-y-3 border-l border-[var(--color-border)] pl-3">
          {comment.children.map((child) => (
            <TaskCommentItem key={child.id} comment={child} onReply={onReply} onDelete={onDelete} />
          ))}
        </div>
      )}
    </div>
  )
}

function TaskDetailDialog({
  projectId,
  taskId,
  moduleOptions,
  onOpenChange,
  onSelectTask,
}: {
  projectId: number
  taskId: number
  moduleOptions: ModuleOption[]
  onOpenChange: (open: boolean) => void
  onSelectTask: (id: number) => void
}) {
  const qc = useQueryClient()
  const [propertyDraft, setPropertyDraft] = useState<TaskPropertyFormState>({})
  const [commentContent, setCommentContent] = useState('')
  const [isEditing, setIsEditing] = useState(false)
  const [editForm, setEditForm] = useState<TaskUpdateRequest | null>(null)

  const { data: detail, isLoading } = useQuery({
    queryKey: ['task-detail', taskId],
    queryFn: () => tasksApi.detail(taskId),
    enabled: taskId > 0,
  })

  const propertyForm = detail
    ? {
        ...buildTaskPropertyForm(detail),
        ...propertyDraft,
      }
    : propertyDraft

  const savePropertyValues = useMutation({
    mutationFn: () => {
      if (!detail) {
        return Promise.resolve()
      }
      return taskPropertyValuesApi.save(taskId, {
        values: detail.propertyValues.map((item) => {
          const formValue = propertyForm[item.propertyId]
          let valueText: string | null
          if (item.propertyType === 'MULTI_SELECT') {
            valueText = JSON.stringify(Array.isArray(formValue) ? formValue : [])
          } else if (typeof formValue === 'string') {
            valueText = formValue.trim() ? formValue.trim() : null
          } else {
            valueText = null
          }
          return {
            propertyId: item.propertyId,
            valueText,
          }
        }),
      })
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['task-detail', taskId] })
    },
  })

  const createComment = useMutation({
    mutationFn: ({ parentId, content }: { parentId?: number; content: string }) =>
      taskCommentsApi.create(taskId, { parentId, content }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['task-detail', taskId] })
      setCommentContent('')
    },
  })

  const deleteComment = useMutation({
    mutationFn: (id: number) => taskCommentsApi.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['task-detail', taskId] })
    },
  })

  const updateStatus = useMutation({
    mutationFn: (status: TaskStatus) => tasksApi.updateStatus(taskId, { targetStatus: status }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-tasks', projectId] })
      qc.invalidateQueries({ queryKey: ['project-stats', projectId] })
      qc.invalidateQueries({ queryKey: ['task-detail', taskId] })
    },
  })

  const updateTask = useMutation({
    mutationFn: (data: TaskUpdateRequest) => tasksApi.update(taskId, data),
    onSuccess: () => {
      setIsEditing(false)
      setEditForm(null)
      qc.invalidateQueries({ queryKey: ['project-tasks', projectId] })
      qc.invalidateQueries({ queryKey: ['project-stats', projectId] })
      qc.invalidateQueries({ queryKey: ['task-detail', taskId] })
    },
  })

  const deleteTask = useMutation({
    mutationFn: () => tasksApi.remove(taskId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-tasks', projectId] })
      qc.invalidateQueries({ queryKey: ['project-stats', projectId] })
      onOpenChange(false)
    },
  })

  const moduleLabelMap = Object.fromEntries(moduleOptions.map((option) => [Number(option.value), option.label.trim()]))

  const footer = detail ? (
    <>
      {detail.propertyValues.length > 0 && (
        <Button variant="secondary" onClick={() => savePropertyValues.mutate()} loading={savePropertyValues.isPending}>
          保存属性值
        </Button>
      )}
      {isEditing && editForm && (
        <>
          <Button
            variant="secondary"
            onClick={() => {
              setEditForm(buildTaskUpdateForm(detail))
              setIsEditing(false)
            }}
          >
            取消编辑
          </Button>
          <Button onClick={() => updateTask.mutate(editForm)} loading={updateTask.isPending}>
            保存任务
          </Button>
        </>
      )}
    </>
  ) : undefined

  return (
    <Dialog open={taskId > 0} onOpenChange={onOpenChange}>
      <DialogContent
        title={detail ? detail.title : '任务详情'}
        description={detail ? detail.taskNo || `#${detail.id}` : '查看任务基础信息、属性和评论'}
        className="max-w-5xl"
        contentClassName="overflow-y-auto pr-1"
        footer={footer}
        headerActions={
          detail ? (
            <>
              <span
                className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${PRIORITY_STYLES[detail.priority]}`}
              >
                {detail.priorityDescription || detail.priority}
              </span>
              <select
                value={detail.status}
                disabled={updateStatus.isPending || updateTask.isPending}
                onChange={(e) => {
                  const nextStatus = e.target.value as TaskStatus
                  if (nextStatus !== detail.status) {
                    updateStatus.mutate(nextStatus)
                  }
                }}
                className="rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-1.5 text-sm text-[var(--color-text)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)]"
              >
                {TASK_STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <Button
                size="sm"
                variant="secondary"
                onClick={() => {
                  if (isEditing) {
                    setIsEditing(false)
                    setEditForm(null)
                    return
                  }
                  setEditForm(buildTaskUpdateForm(detail))
                  setIsEditing(true)
                }}
              >
                {isEditing ? '退出编辑' : '编辑任务'}
              </Button>
              <Button
                size="sm"
                variant="danger"
                onClick={() => {
                  if (!window.confirm(`确认删除任务“${detail.title}”吗？`)) return
                  deleteTask.mutate()
                }}
                loading={deleteTask.isPending}
              >
                删除任务
              </Button>
            </>
          ) : undefined
        }
      >
        {isLoading || !detail ? (
          <div className="py-12 text-sm text-center text-[var(--color-text-muted)]">正在加载任务详情...</div>
        ) : (
          <div className="space-y-6">
            {isEditing && editForm ? (
              <section className="space-y-4 rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
                <div>
                  <h3 className="text-sm font-medium text-[var(--color-text)]">编辑任务</h3>
                  <p className="mt-1 text-xs text-[var(--color-text-muted)]">任务基本信息和状态在这里统一维护。</p>
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <Input
                    label="任务标题"
                    value={editForm.title}
                    onChange={(e) => setEditForm((current) => current ? { ...current, title: e.target.value } : current)}
                    required
                  />
                  <Select
                    label="任务类型"
                    value={editForm.taskType}
                    onChange={(e) =>
                      setEditForm((current) => current ? { ...current, taskType: e.target.value as TaskType } : current)
                    }
                    options={TASK_TYPE_OPTIONS}
                  />
                  <Select
                    label="所属模块"
                    value={editForm.moduleId ? String(editForm.moduleId) : ''}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current
                          ? { ...current, moduleId: parseOptionalNumber(e.target.value) }
                          : current,
                      )
                    }
                    options={[{ value: '', label: '不指定模块' }, ...moduleOptions]}
                  />
                  <Input
                    label="父任务 ID"
                    type="number"
                    value={editForm.parentTaskId ?? ''}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current
                          ? { ...current, parentTaskId: parseOptionalNumber(e.target.value) }
                          : current,
                      )
                    }
                    placeholder="可选"
                  />
                  <Select
                    label="状态"
                    value={editForm.status}
                    onChange={(e) =>
                      setEditForm((current) => current ? { ...current, status: e.target.value as TaskStatus } : current)
                    }
                    options={TASK_STATUS_OPTIONS}
                  />
                  <Select
                    label="优先级"
                    value={editForm.priority}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current
                          ? { ...current, priority: e.target.value as TaskPriority }
                          : current,
                      )
                    }
                    options={TASK_PRIORITY_OPTIONS}
                  />
                  <Input
                    label="负责人 ID"
                    type="number"
                    value={editForm.assigneeId ?? ''}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current ? { ...current, assigneeId: parseOptionalNumber(e.target.value) } : current,
                      )
                    }
                    placeholder="可选"
                  />
                  <Input
                    label="报告人 ID"
                    type="number"
                    value={editForm.reporterId ?? ''}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current ? { ...current, reporterId: parseOptionalNumber(e.target.value) } : current,
                      )
                    }
                    placeholder="可选"
                  />
                  <Input
                    label="截止时间"
                    type="datetime-local"
                    value={editForm.dueDate ?? ''}
                    onChange={(e) =>
                      setEditForm((current) => current ? { ...current, dueDate: e.target.value || undefined } : current)
                    }
                  />
                  <Input
                    label="排序值"
                    type="number"
                    min="0"
                    value={editForm.sortOrder ?? 0}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current
                          ? { ...current, sortOrder: parseOptionalNumber(e.target.value) ?? 0 }
                          : current,
                      )
                    }
                  />
                  <Input
                    label="预估工时"
                    type="number"
                    step="0.1"
                    min="0"
                    value={editForm.estimatedHours ?? ''}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current
                          ? { ...current, estimatedHours: parseOptionalNumber(e.target.value) }
                          : current,
                      )
                    }
                    placeholder="可选"
                  />
                  <Input
                    label="实际工时"
                    type="number"
                    step="0.1"
                    min="0"
                    value={editForm.actualHours ?? ''}
                    onChange={(e) =>
                      setEditForm((current) =>
                        current ? { ...current, actualHours: parseOptionalNumber(e.target.value) } : current,
                      )
                    }
                    placeholder="可选"
                  />
                </div>
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-medium text-[var(--color-text)]">描述</label>
                  <textarea
                    value={editForm.description ?? ''}
                    onChange={(e) =>
                      setEditForm((current) => current ? { ...current, description: e.target.value } : current)
                    }
                    rows={4}
                    className="w-full resize-none rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm text-[var(--color-text)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]"
                  />
                </div>
              </section>
            ) : (
              <>
                <section className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
                    <p className="mb-2 text-xs uppercase tracking-wide text-[var(--color-text-subtle)]">基础信息</p>
                    <div className="space-y-2 text-sm">
                      <p><span className="text-[var(--color-text-muted)]">标题：</span>{detail.title}</p>
                      <p><span className="text-[var(--color-text-muted)]">编号：</span>{detail.taskNo || '-'}</p>
                      <p><span className="text-[var(--color-text-muted)]">类型：</span>{detail.taskTypeDescription || detail.taskType}</p>
                      <p><span className="text-[var(--color-text-muted)]">状态：</span>{detail.statusDescription || detail.status}</p>
                      <p><span className="text-[var(--color-text-muted)]">优先级：</span>{detail.priorityDescription || detail.priority}</p>
                      <p><span className="text-[var(--color-text-muted)]">模块：</span>{detail.moduleId ? (moduleLabelMap[detail.moduleId] ?? `#${detail.moduleId}`) : '未设置'}</p>
                      <p><span className="text-[var(--color-text-muted)]">父任务：</span>{detail.parentTaskId ?? '-'}</p>
                      <p><span className="text-[var(--color-text-muted)]">负责人：</span>{detail.assigneeId ?? '-'}</p>
                      <p><span className="text-[var(--color-text-muted)]">报告人：</span>{detail.reporterId ?? '-'}</p>
                    </div>
                  </div>
                  <div className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
                    <p className="mb-2 text-xs uppercase tracking-wide text-[var(--color-text-subtle)]">时间与工时</p>
                    <div className="space-y-2 text-sm">
                      <p><span className="text-[var(--color-text-muted)]">截止时间：</span>{formatDateTime(detail.dueDate)}</p>
                      <p><span className="text-[var(--color-text-muted)]">开始时间：</span>{formatDateTime(detail.startedAt)}</p>
                      <p><span className="text-[var(--color-text-muted)]">完成时间：</span>{formatDateTime(detail.finishedAt)}</p>
                      <p><span className="text-[var(--color-text-muted)]">预估工时：</span>{detail.estimatedHours ?? '-'}</p>
                      <p><span className="text-[var(--color-text-muted)]">实际工时：</span>{detail.actualHours ?? '-'}</p>
                    </div>
                  </div>
                </section>

                <section className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
                  <p className="mb-2 text-xs uppercase tracking-wide text-[var(--color-text-subtle)]">描述</p>
                  <p className="whitespace-pre-wrap text-sm text-[var(--color-text)]">
                    {detail.description || '暂无描述'}
                  </p>
                </section>
              </>
            )}

            <section className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
              <div className="flex items-center justify-between gap-3 mb-4">
                <div>
                  <h3 className="text-sm font-medium text-[var(--color-text)]">自定义属性</h3>
                  <p className="text-xs text-[var(--color-text-muted)]">支持直接保存当前任务属性值</p>
                </div>
              </div>
              {detail.propertyValues.length === 0 ? (
                <p className="text-sm text-[var(--color-text-subtle)]">当前项目未配置任务属性。</p>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {detail.propertyValues.map((property) => (
                    <div key={property.propertyId} className="space-y-1.5">
                      <label className="text-sm font-medium text-[var(--color-text)]">
                        {property.propertyName}
                        {property.required && <span className="text-red-500 ml-1">*</span>}
                      </label>
                      {renderTaskPropertyInput(
                        property,
                        propertyForm[property.propertyId] ?? '',
                        (nextValue) =>
                          setPropertyDraft((current) => ({
                            ...current,
                            [property.propertyId]: nextValue,
                          })),
                      )}
                    </div>
                  ))}
                </div>
              )}
            </section>

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <div className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
                <h3 className="text-sm font-medium text-[var(--color-text)] mb-4">子任务</h3>
                {detail.children.length === 0 ? (
                  <p className="text-sm text-[var(--color-text-subtle)]">暂无子任务</p>
                ) : (
                  <div className="space-y-3">
                    {detail.children.map((child) => (
                      <div
                        key={child.id}
                        role="button"
                        tabIndex={0}
                        onClick={() => onSelectTask(child.id)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' || e.key === ' ') {
                            e.preventDefault()
                            onSelectTask(child.id)
                          }
                        }}
                        className="rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] p-3 transition-colors hover:border-[var(--color-primary)]"
                      >
                        <div className="flex items-center justify-between gap-3">
                          <div>
                            <p className="text-sm font-medium text-[var(--color-text)]">{child.title}</p>
                            <p className="text-xs text-[var(--color-text-subtle)] mt-1">
                              {child.taskTypeDescription || child.taskType} · {child.statusDescription || child.status}
                            </p>
                          </div>
                          <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${PRIORITY_STYLES[child.priority]}`}>
                            {child.priorityDescription || child.priority}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
                <h3 className="text-sm font-medium text-[var(--color-text)] mb-4">活动日志</h3>
                {detail.activities.length === 0 ? (
                  <p className="text-sm text-[var(--color-text-subtle)]">暂无活动</p>
                ) : (
                  <div className="space-y-3">
                    {detail.activities.map((activity) => (
                      <div key={activity.id} className="border-l-2 border-[var(--color-primary)] pl-3">
                        <p className="text-sm text-[var(--color-text)]">{activity.actionContent}</p>
                        <p className="text-xs text-[var(--color-text-subtle)] mt-1">
                          {activity.actionTypeDescription || activity.actionType} · 用户 {activity.createdBy ?? '-'} · {formatDateTime(activity.createdAt)}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </section>

            <section className="rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface-2)] p-4">
              <h3 className="text-sm font-medium text-[var(--color-text)] mb-4">评论</h3>
              <form
                className="space-y-3 mb-4"
                onSubmit={(e) => {
                  e.preventDefault()
                  const content = commentContent.trim()
                  if (!content) return
                  createComment.mutate({ content })
                }}
              >
                <textarea
                  value={commentContent}
                  onChange={(e) => setCommentContent(e.target.value)}
                  rows={3}
                  placeholder="写下评论内容"
                  className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
                />
                <div className="flex justify-end">
                  <Button size="sm" type="submit" loading={createComment.isPending}>
                    发表评论
                  </Button>
                </div>
              </form>

              {detail.comments.length === 0 ? (
                <p className="text-sm text-[var(--color-text-subtle)]">暂无评论</p>
              ) : (
                <div className="space-y-3">
                  {detail.comments.map((comment) => (
                    <TaskCommentItem
                      key={comment.id}
                      comment={comment}
                      onReply={(parentId, content) => createComment.mutate({ parentId, content })}
                      onDelete={(id) => deleteComment.mutate(id)}
                    />
                  ))}
                </div>
              )}
            </section>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}

function TaskCard({
  task,
  onStatusChange,
  onOpenDetail,
}: {
  task: Task
  onStatusChange: (id: number, status: TaskStatus) => void
  onOpenDetail: (id: number) => void
}) {
  const nextStatus: Partial<Record<TaskStatus, TaskStatus>> = {
    TODO: 'IN_PROGRESS',
    IN_PROGRESS: 'TESTING',
    TESTING: 'DONE',
  }
  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => onOpenDetail(task.id)}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault()
          onOpenDetail(task.id)
        }
      }}
      className="w-full text-left bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-3.5 shadow-sm hover:shadow-md transition-shadow"
    >
      <p className="text-sm font-medium text-[var(--color-text)] mb-2 leading-snug">{task.title}</p>
      {task.description && (
        <p className="text-xs text-[var(--color-text-muted)] mb-3 line-clamp-2">{task.description}</p>
      )}
      <div className="flex items-center gap-2 text-xs text-[var(--color-text-subtle)] mb-3">
        <span>{task.taskTypeDescription || task.taskType}</span>
        <span>·</span>
        <span>{task.taskNo || `#${task.id}`}</span>
      </div>
      <div className="flex items-center justify-between">
        <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${PRIORITY_STYLES[task.priority]}`}>
          <Flag size={10} className="inline mr-1" />
          {task.priorityDescription}
        </span>
        {nextStatus[task.status] && (
          <button
            type="button"
            className="text-xs text-[var(--color-primary)] hover:underline"
            onClick={(e) => {
              e.stopPropagation()
              onStatusChange(task.id, nextStatus[task.status]!)
            }}
          >
            Move →
          </button>
        )}
      </div>
    </div>
  )
}

function TaskBoard({ projectId }: { projectId: number }) {
  const qc = useQueryClient()
  const [createOpen, setCreateOpen] = useState(false)
  const [selectedTaskId, setSelectedTaskId] = useState<number | null>(null)
  const [taskForm, setTaskForm] = useState<TaskCreateFormState>(createEmptyTaskForm())

  const { data: tasks = [] } = useQuery({
    queryKey: ['project-tasks', projectId],
    queryFn: () => projectsApi.tasks(projectId),
    select: (d) => d ?? [],
  })

  const { data: modules = [] } = useQuery({
    queryKey: ['project-modules-tree', projectId],
    queryFn: () => projectModulesApi.tree(projectId),
    select: (data) => data ?? [],
  })

  const moduleOptions = flattenModuleOptions(modules)

  const createTask = useMutation({
    mutationFn: (data: TaskCreateRequest) => tasksApi.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-tasks', projectId] })
      qc.invalidateQueries({ queryKey: ['project-stats', projectId] })
      setCreateOpen(false)
      setTaskForm(createEmptyTaskForm())
    },
  })

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: TaskStatus }) =>
      tasksApi.updateStatus(id, { targetStatus: status }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-tasks', projectId] })
      qc.invalidateQueries({ queryKey: ['project-stats', projectId] })
      if (selectedTaskId) {
        qc.invalidateQueries({ queryKey: ['task-detail', selectedTaskId] })
      }
    },
  })

  const handleStatusChange = (id: number, status: TaskStatus) => {
    updateStatus.mutate({ id, status })
  }

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button size="sm">
              <Plus size={14} />
              新建任务
            </Button>
          </DialogTrigger>
          <DialogContent
            title="新建任务"
            description="填写任务基础信息后即可创建。"
            className="max-w-3xl"
            contentClassName="overflow-y-auto pr-1"
            footer={
              <>
                <Button variant="secondary" type="button" onClick={() => setCreateOpen(false)}>
                  取消
                </Button>
                <Button type="submit" form="create-task-form" loading={createTask.isPending}>
                  创建任务
                </Button>
              </>
            }
          >
            <form
              id="create-task-form"
              onSubmit={(e) => {
                e.preventDefault()
                createTask.mutate({ ...taskForm, projectId })
              }}
              className="space-y-4"
            >
              <Input
                label="任务标题"
                value={taskForm.title}
                onChange={(e) => setTaskForm((f) => ({ ...f, title: e.target.value }))}
                placeholder="请输入任务标题"
                required
              />
              <Select
                label="任务类型"
                value={taskForm.taskType}
                onChange={(e) => setTaskForm((f) => ({ ...f, taskType: e.target.value as TaskType }))}
                options={TASK_TYPE_OPTIONS}
              />
              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-[var(--color-text)]">描述</label>
                <textarea
                  value={taskForm.description}
                  onChange={(e) => setTaskForm((f) => ({ ...f, description: e.target.value }))}
                  rows={3}
                  className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <Select
                  label="所属模块"
                  value={taskForm.moduleId ? String(taskForm.moduleId) : ''}
                  onChange={(e) => setTaskForm((f) => ({ ...f, moduleId: parseOptionalNumber(e.target.value) }))}
                  options={[{ value: '', label: '不指定模块' }, ...moduleOptions]}
                />
                <Input
                  label="父任务 ID"
                  type="number"
                  value={taskForm.parentId ?? ''}
                  onChange={(e) => setTaskForm((f) => ({ ...f, parentId: parseOptionalNumber(e.target.value) }))}
                  placeholder="可选"
                />
              </div>
              <Select
                label="优先级"
                value={taskForm.priority}
                onChange={(e) => setTaskForm((f) => ({ ...f, priority: e.target.value as TaskPriority }))}
                options={TASK_PRIORITY_OPTIONS}
              />
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="负责人 ID"
                  type="number"
                  value={taskForm.assigneeId ?? ''}
                  onChange={(e) => setTaskForm((f) => ({ ...f, assigneeId: parseOptionalNumber(e.target.value) }))}
                  placeholder="可选"
                />
                <Input
                  label="报告人 ID"
                  type="number"
                  value={taskForm.reporterId ?? ''}
                  onChange={(e) => setTaskForm((f) => ({ ...f, reporterId: parseOptionalNumber(e.target.value) }))}
                  placeholder="可选"
                />
              </div>
              <Input
                label="截止时间"
                type="datetime-local"
                value={taskForm.dueDate ?? ''}
                onChange={(e) => setTaskForm((f) => ({ ...f, dueDate: e.target.value || undefined }))}
              />
              <div className="grid grid-cols-3 gap-3">
                <Input
                  label="预估工时"
                  type="number"
                  step="0.1"
                  min="0"
                  value={taskForm.estimatedHours ?? ''}
                  onChange={(e) =>
                    setTaskForm((f) => ({ ...f, estimatedHours: parseOptionalNumber(e.target.value) }))
                  }
                  placeholder="可选"
                />
                <Input
                  label="实际工时"
                  type="number"
                  step="0.1"
                  min="0"
                  value={taskForm.actualHours ?? ''}
                  onChange={(e) =>
                    setTaskForm((f) => ({ ...f, actualHours: parseOptionalNumber(e.target.value) }))
                  }
                  placeholder="可选"
                />
                <Input
                  label="排序值"
                  type="number"
                  min="0"
                  value={taskForm.sortOrder ?? 0}
                  onChange={(e) => setTaskForm((f) => ({ ...f, sortOrder: parseOptionalNumber(e.target.value) ?? 0 }))}
                />
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        {COLUMNS.map(({ status, label }) => {
          const col = tasks.filter((t) => t.status === status)
          return (
            <div key={status} className="flex flex-col gap-3">
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wider">
                  {label}
                </span>
                <span className="text-xs bg-[var(--color-surface-3)] text-[var(--color-text-muted)] px-1.5 py-0.5 rounded-full">
                  {col.length}
                </span>
              </div>
              <div className="flex flex-col gap-2 min-h-24">
                {col.map((task) => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      onStatusChange={handleStatusChange}
                      onOpenDetail={setSelectedTaskId}
                    />
                ))}
              </div>
            </div>
          )
        })}
      </div>

        {selectedTaskId !== null && (
          <TaskDetailDialog
            key={selectedTaskId}
            projectId={projectId}
            taskId={selectedTaskId}
            moduleOptions={moduleOptions}
            onOpenChange={(open) => !open && setSelectedTaskId(null)}
            onSelectTask={setSelectedTaskId}
          />
        )}
    </div>
  )
}

function TaskPropertyFormFields({
  form,
  setForm,
}: {
  form: TaskPropertyCreateRequest
  setForm: Dispatch<SetStateAction<TaskPropertyCreateRequest>>
}) {
  const showOptionsJson = form.propertyType === 'SELECT' || form.propertyType === 'MULTI_SELECT'

  return (
    <>
      <Input
        label="属性名称"
        value={form.name}
        onChange={(e) => setForm((current) => ({ ...current, name: e.target.value }))}
        required
      />
      <Input
        label="属性编码"
        value={form.code}
        onChange={(e) => setForm((current) => ({ ...current, code: e.target.value }))}
        required
      />
      <Select
        label="属性类型"
        value={form.propertyType}
        onChange={(e) =>
          setForm((current) => ({
            ...current,
            propertyType: e.target.value as TaskPropertyType,
          }))
        }
        options={TASK_PROPERTY_TYPE_OPTIONS}
      />
      <Input
        label="排序"
        type="number"
        value={String(form.sortOrder ?? 0)}
        onChange={(e) =>
          setForm((current) => ({
            ...current,
            sortOrder: Number(e.target.value || 0),
          }))
        }
      />
      <div className="grid grid-cols-2 gap-3">
        <label className="inline-flex items-center gap-2 text-sm text-[var(--color-text)]">
          <input
            type="checkbox"
            checked={form.required ?? false}
            onChange={(e) => setForm((current) => ({ ...current, required: e.target.checked }))}
          />
          必填
        </label>
        <label className="inline-flex items-center gap-2 text-sm text-[var(--color-text)]">
          <input
            type="checkbox"
            checked={form.enabled ?? true}
            onChange={(e) => setForm((current) => ({ ...current, enabled: e.target.checked }))}
          />
          启用
        </label>
      </div>
      <div className="flex flex-col gap-1.5">
        <label className="text-sm font-medium text-[var(--color-text)]">
          选项 JSON {showOptionsJson ? '' : '(可选)'}
        </label>
        <textarea
          rows={4}
          value={form.optionsJson ?? ''}
          onChange={(e) => setForm((current) => ({ ...current, optionsJson: e.target.value }))}
          placeholder='例如：[{"label":"高","value":"HIGH"}]'
          className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
        />
      </div>
    </>
  )
}

function TaskPropertiesTab({ projectId }: { projectId: number }) {
  const qc = useQueryClient()
  const [createOpen, setCreateOpen] = useState(false)
  const [editing, setEditing] = useState<TaskProperty | null>(null)
  const emptyForm = (): TaskPropertyCreateRequest => ({
    name: '',
    code: '',
    propertyType: 'TEXT',
    required: false,
    optionsJson: '',
    sortOrder: 0,
    enabled: true,
  })
  const [createForm, setCreateForm] = useState<TaskPropertyCreateRequest>(emptyForm)
  const [editForm, setEditForm] = useState<TaskPropertyCreateRequest>(emptyForm)

  const { data: properties = [] } = useQuery({
    queryKey: ['project-task-properties', projectId],
    queryFn: () => taskPropertiesApi.list(projectId),
    select: (data) => data ?? [],
  })

  const createProperty = useMutation({
    mutationFn: (data: TaskPropertyCreateRequest) => taskPropertiesApi.create(projectId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-task-properties', projectId] })
      qc.invalidateQueries({ queryKey: ['task-detail'] })
      setCreateOpen(false)
      setCreateForm(emptyForm())
    },
  })

  const updateProperty = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TaskPropertyCreateRequest }) =>
      taskPropertiesApi.update(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-task-properties', projectId] })
      qc.invalidateQueries({ queryKey: ['task-detail'] })
      setEditing(null)
    },
  })

  const deleteProperty = useMutation({
    mutationFn: (id: number) => taskPropertiesApi.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-task-properties', projectId] })
      qc.invalidateQueries({ queryKey: ['task-detail'] })
    },
  })

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button size="sm">
              <Plus size={14} />
              新增属性
            </Button>
          </DialogTrigger>
          <DialogContent title="新增任务属性">
            <form
              className="space-y-4"
              onSubmit={(e) => {
                e.preventDefault()
                createProperty.mutate(createForm)
              }}
            >
              <TaskPropertyFormFields form={createForm} setForm={setCreateForm} />
              <div className="flex justify-end gap-2 pt-2">
                <Button size="sm" variant="secondary" type="button" onClick={() => setCreateOpen(false)}>
                  取消
                </Button>
                <Button size="sm" type="submit" loading={createProperty.isPending}>
                  保存
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Dialog open={editing !== null} onOpenChange={(open) => !open && setEditing(null)}>
        <DialogContent title="编辑任务属性">
          <form
            className="space-y-4"
            onSubmit={(e) => {
              e.preventDefault()
              if (!editing) return
              updateProperty.mutate({ id: editing.id, data: editForm })
            }}
          >
            <TaskPropertyFormFields form={editForm} setForm={setEditForm} />
            <div className="flex justify-end gap-2 pt-2">
              <Button size="sm" variant="secondary" type="button" onClick={() => setEditing(null)}>
                取消
              </Button>
              <Button size="sm" type="submit" loading={updateProperty.isPending}>
                保存
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      {properties.length === 0 ? (
        <p className="text-sm text-center text-[var(--color-text-subtle)] py-12">当前项目还没有任务属性。</p>
      ) : (
        <div className="space-y-3">
          {properties.map((property) => (
            <div
              key={property.id}
              className="bg-[var(--color-surface-2)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-4"
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <p className="text-sm font-medium text-[var(--color-text)]">{property.name}</p>
                    <span className="text-xs px-2 py-0.5 rounded-full bg-[var(--color-surface-3)] text-[var(--color-text-muted)]">
                      {property.propertyTypeDescription || property.propertyType}
                    </span>
                    {!property.enabled && (
                      <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">
                        已停用
                      </span>
                    )}
                    {property.required && (
                      <span className="text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-600">
                        必填
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-[var(--color-text-subtle)]">code: {property.code}</p>
                  {property.optionsJson && (
                    <pre className="mt-2 text-xs text-[var(--color-text-muted)] whitespace-pre-wrap break-all">
                      {property.optionsJson}
                    </pre>
                  )}
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => {
                      setEditForm({
                        name: property.name,
                        code: property.code,
                        propertyType: property.propertyType,
                        required: property.required,
                        optionsJson: property.optionsJson ?? '',
                        sortOrder: property.sortOrder,
                        enabled: property.enabled,
                      })
                      setEditing(property)
                    }}
                  >
                    编辑
                  </Button>
                  <Button
                    size="sm"
                    variant="danger"
                    onClick={() => {
                      if (confirm(`删除属性 "${property.name}"?`)) {
                        deleteProperty.mutate(property.id)
                      }
                    }}
                    loading={deleteProperty.isPending}
                  >
                    <Trash2 size={12} />
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function RepoTab({ projectId }: { projectId: number }) {
  const [createOpen, setCreateOpen] = useState(false)
  const qc = useQueryClient()
  const { data: repos = [] } = useQuery({
    queryKey: ['repos', projectId],
    queryFn: () => reposApi.list(projectId),
    select: (d) => d ?? [],
  })

  const createRepo = useMutation({
    mutationFn: reposApi.create,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['repos', projectId] })
      setCreateOpen(false)
    },
  })

  const [repoForm, setRepoForm] = useState({
    repoName: '',
    repoUrl: '',
    defaultBranch: 'main',
    repoType: 'GITHUB' as const,
    description: '',
  })

  const REPO_TYPE_STYLES: Record<string, string> = {
    GITHUB: 'bg-gray-100 text-gray-700',
    GITLAB: 'bg-orange-50 text-orange-700',
    GITEE: 'bg-red-50 text-red-700',
    CUSTOM: 'bg-purple-50 text-purple-700',
  }

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button size="sm">
              <Plus size={14} />
              Link Repository
            </Button>
          </DialogTrigger>
          <DialogContent title="Link Repository">
            <form
              onSubmit={(e) => {
                e.preventDefault()
                createRepo.mutate({ ...repoForm, projectId })
              }}
              className="space-y-4"
            >
              <Input
                label="Repository Name"
                value={repoForm.repoName}
                onChange={(e) => setRepoForm((f) => ({ ...f, repoName: e.target.value }))}
                required
              />
              <Input
                label="Repository URL"
                value={repoForm.repoUrl}
                onChange={(e) => setRepoForm((f) => ({ ...f, repoUrl: e.target.value }))}
                placeholder="https://github.com/org/repo"
                required
              />
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Default Branch"
                  value={repoForm.defaultBranch}
                  onChange={(e) => setRepoForm((f) => ({ ...f, defaultBranch: e.target.value }))}
                />
                <Select
                  label="Type"
                  value={repoForm.repoType}
                  onChange={(e) => setRepoForm((f) => ({ ...f, repoType: e.target.value as typeof repoForm.repoType }))}
                  options={[
                    { value: 'GITHUB', label: 'GitHub' },
                    { value: 'GITLAB', label: 'GitLab' },
                    { value: 'GITEE', label: 'Gitee' },
                    { value: 'CUSTOM', label: 'Custom' },
                  ]}
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="secondary" type="button" onClick={() => setCreateOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" loading={createRepo.isPending}>
                  Link
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>
      {repos.length === 0 ? (
        <p className="text-sm text-center text-[var(--color-text-subtle)] py-12">No repositories linked yet.</p>
      ) : (
        <div className="space-y-3">
          {repos.map((r) => (
            <div key={r.id} className="bg-[var(--color-surface-2)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-[var(--color-text)]">{r.repoName}</p>
                <a href={r.repoUrl} target="_blank" rel="noopener noreferrer" className="text-xs text-[var(--color-primary)] hover:underline">{r.repoUrl}</a>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-[var(--color-text-muted)]">{r.defaultBranch}</span>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${REPO_TYPE_STYLES[r.repoType] ?? 'bg-gray-100 text-gray-700'}`}>
                  {r.repoTypeDescription}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

const CREDENTIAL_TYPE_STYLES: Record<string, string> = {
  USERNAME_PASSWORD: 'bg-blue-50 text-blue-700',
  TOKEN: 'bg-purple-50 text-purple-700',
}

function CredentialsTab({ projectId }: { projectId: number }) {
  const qc = useQueryClient()
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<Credential | null>(null)

  const emptyCreate = (): CredentialCreateRequest => ({
    projectId,
    name: '',
    credentialType: 'TOKEN',
    username: '',
    secretValue: '',
    description: '',
  })
  const [createForm, setCreateForm] = useState<CredentialCreateRequest>(emptyCreate)
  const [editForm, setEditForm] = useState<CredentialUpdateRequest>({
    name: '',
    credentialType: 'TOKEN',
    username: '',
    secretValue: '',
    description: '',
  })

  const { data: credentials = [] } = useQuery({
    queryKey: ['credentials', projectId],
    queryFn: () => pipelineApi.listCredentials(projectId),
    select: (d) => d ?? [],
  })

  const createCred = useMutation({
    mutationFn: (data: CredentialCreateRequest) => pipelineApi.createCredential(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['credentials', projectId] })
      setCreateOpen(false)
      setCreateForm(emptyCreate())
    },
  })

  const updateCred = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CredentialUpdateRequest }) =>
      pipelineApi.updateCredential(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['credentials', projectId] })
      setEditTarget(null)
    },
  })

  const deleteCred = useMutation({
    mutationFn: (id: number) => pipelineApi.removeCredential(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['credentials', projectId] })
    },
  })

  const CREDENTIAL_TYPE_OPTIONS = [
    { value: 'TOKEN', label: 'Token' },
    { value: 'USERNAME_PASSWORD', label: '用户名密码' },
  ]

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button size="sm">
              <Plus size={14} />
              Add Credential
            </Button>
          </DialogTrigger>
          <DialogContent title="Add Credential">
            <form
              onSubmit={(e) => {
                e.preventDefault()
                createCred.mutate(createForm)
              }}
              className="space-y-4"
            >
              <Input
                label="Name"
                value={createForm.name}
                onChange={(e) => setCreateForm((f) => ({ ...f, name: e.target.value }))}
                required
              />
              <Select
                label="Type"
                value={createForm.credentialType}
                onChange={(e) =>
                  setCreateForm((f) => ({
                    ...f,
                    credentialType: e.target.value as CredentialCreateRequest['credentialType'],
                  }))
                }
                options={CREDENTIAL_TYPE_OPTIONS}
              />
              {createForm.credentialType === 'USERNAME_PASSWORD' && (
                <Input
                  label="Username"
                  value={createForm.username ?? ''}
                  onChange={(e) => setCreateForm((f) => ({ ...f, username: e.target.value }))}
                />
              )}
              <Input
                label="Secret / Token"
                type="password"
                value={createForm.secretValue}
                onChange={(e) => setCreateForm((f) => ({ ...f, secretValue: e.target.value }))}
                required
              />
              <Input
                label="Description"
                value={createForm.description ?? ''}
                onChange={(e) => setCreateForm((f) => ({ ...f, description: e.target.value }))}
              />
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="secondary" type="button" onClick={() => setCreateOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" loading={createCred.isPending}>
                  Add
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Dialog open={editTarget !== null} onOpenChange={(open) => { if (!open) setEditTarget(null) }}>
        <DialogContent title="Edit Credential">
          <form
            onSubmit={(e) => {
              e.preventDefault()
              if (editTarget) updateCred.mutate({ id: editTarget.id, data: editForm })
            }}
            className="space-y-4"
          >
            <Input
              label="Name"
              value={editForm.name}
              onChange={(e) => setEditForm((f) => ({ ...f, name: e.target.value }))}
              required
            />
            <Select
              label="Type"
              value={editForm.credentialType}
              onChange={(e) =>
                setEditForm((f) => ({
                  ...f,
                  credentialType: e.target.value as CredentialUpdateRequest['credentialType'],
                }))
              }
              options={CREDENTIAL_TYPE_OPTIONS}
            />
            {editForm.credentialType === 'USERNAME_PASSWORD' && (
              <Input
                label="Username"
                value={editForm.username ?? ''}
                onChange={(e) => setEditForm((f) => ({ ...f, username: e.target.value }))}
              />
            )}
            <Input
              label="New Secret / Token"
              type="password"
              value={editForm.secretValue ?? ''}
              onChange={(e) => setEditForm((f) => ({ ...f, secretValue: e.target.value }))}
              placeholder="Leave blank to keep unchanged"
            />
            <Input
              label="Description"
              value={editForm.description ?? ''}
              onChange={(e) => setEditForm((f) => ({ ...f, description: e.target.value }))}
            />
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="secondary" type="button" onClick={() => setEditTarget(null)}>
                Cancel
              </Button>
              <Button type="submit" loading={updateCred.isPending}>
                Save
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      {credentials.length === 0 ? (
        <p className="text-sm text-center text-[var(--color-text-subtle)] py-12">No credentials added yet.</p>
      ) : (
        <div className="space-y-3">
          {credentials.map((cred) => (
            <div
              key={cred.id}
              className="bg-[var(--color-surface-2)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-4 flex items-center justify-between"
            >
              <div className="flex items-center gap-3">
                <KeyRound size={16} className="text-[var(--color-text-muted)] shrink-0" />
                <div>
                  <p className="text-sm font-medium text-[var(--color-text)]">{cred.name}</p>
                  {cred.description && (
                    <p className="text-xs text-[var(--color-text-muted)] mt-0.5">{cred.description}</p>
                  )}
                  {cred.username && (
                    <p className="text-xs text-[var(--color-text-subtle)] mt-0.5 font-mono">{cred.username}</p>
                  )}
                </div>
              </div>
              <div className="flex items-center gap-2">
                <span
                  className={`text-xs px-2 py-0.5 rounded-full font-medium ${CREDENTIAL_TYPE_STYLES[cred.credentialType] ?? 'bg-gray-100 text-gray-700'}`}
                >
                  {cred.credentialTypeDescription || cred.credentialType}
                </span>
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => {
                    setEditForm({
                      name: cred.name,
                      credentialType: cred.credentialType,
                      username: cred.username ?? '',
                      secretValue: '',
                      description: cred.description ?? '',
                    })
                    setEditTarget(cred)
                  }}
                >
                  Edit
                </Button>
                <Button
                  size="sm"
                  variant="danger"
                  onClick={() => {
                    if (confirm(`Delete credential "${cred.name}"?`)) deleteCred.mutate(cred.id)
                  }}
                  loading={deleteCred.isPending}
                >
                  <Trash2 size={12} />
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const projectId = Number(id)

  const { data: project, isLoading } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectsApi.get(projectId),
  })

  const { data: stats } = useQuery({
    queryKey: ['project-stats', projectId],
    queryFn: () => projectsApi.taskStats(projectId),
  })

  const deleteProject = useMutation({
    mutationFn: () => projectsApi.remove(projectId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['projects'] })
      navigate('/projects')
    },
  })

  if (isLoading) {
    return <div className="p-8 animate-pulse h-64 bg-[var(--color-surface)] rounded-[var(--radius-xl)] m-8" />
  }

  if (!project) return null

  const STATUS_STYLES: Record<string, string> = {
    PLANNING: 'bg-slate-100 text-slate-600',
    DEVELOPING: 'bg-blue-50 text-blue-700',
    TESTING: 'bg-amber-50 text-amber-700',
    RELEASED: 'bg-emerald-50 text-emerald-700',
    ARCHIVED: 'bg-gray-100 text-gray-500',
  }

  return (
    <div className="p-8 max-w-6xl mx-auto">
      {/* Back */}
      <button
        onClick={() => navigate('/projects')}
        className="flex items-center gap-1.5 text-sm text-[var(--color-text-muted)] hover:text-[var(--color-text)] mb-6 transition-colors"
      >
        <ArrowLeft size={14} />
        Projects
      </button>

      {/* Header */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <h1 className="text-xl font-semibold text-[var(--color-text)]">{project.name}</h1>
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${STATUS_STYLES[project.status]}`}>
              {project.statusDescription}
            </span>
          </div>
          <p className="text-sm text-[var(--color-text-muted)]">
            {project.description || 'No description provided.'}
          </p>
        </div>
        <Button
          variant="danger"
          size="sm"
          onClick={() => {
            if (confirm(`Delete project "${project.name}"?`)) deleteProject.mutate()
          }}
          loading={deleteProject.isPending}
        >
          <Trash2 size={14} />
          Delete
        </Button>
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-3 sm:grid-cols-6 gap-3 mb-6">
          {[
            { label: 'Total', value: stats.totalTasks },
            { label: 'Todo', value: stats.todoCount },
            { label: 'In Progress', value: stats.inProgressCount },
            { label: 'Testing', value: stats.testingCount },
            { label: 'Done', value: stats.doneCount },
            { label: 'Cancelled', value: stats.cancelledCount },
          ].map(({ label, value }) => (
            <div key={label} className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-3 text-center">
              <p className="text-xl font-semibold text-[var(--color-text)]">{value}</p>
              <p className="text-xs text-[var(--color-text-muted)] mt-0.5">{label}</p>
            </div>
          ))}
        </div>
      )}

      {/* Tabs */}
      <Tabs.Root defaultValue="tasks">
        <Tabs.List className="flex gap-1 border-b border-[var(--color-border)] mb-6">
          {[
            { value: 'tasks', label: 'Tasks' },
            { value: 'task-properties', label: 'Task Properties' },
            { value: 'repos', label: 'Repositories' },
            { value: 'credentials', label: 'Credentials' },
          ].map(({ value, label }) => (
            <Tabs.Trigger
              key={value}
              value={value}
              className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-muted)] border-b-2 border-transparent -mb-px transition-colors data-[state=active]:border-[var(--color-primary)] data-[state=active]:text-[var(--color-primary)]"
            >
              {label}
            </Tabs.Trigger>
          ))}
        </Tabs.List>
        <Tabs.Content value="tasks">
          <TaskBoard projectId={projectId} />
        </Tabs.Content>
        <Tabs.Content value="task-properties">
          <TaskPropertiesTab projectId={projectId} />
        </Tabs.Content>
        <Tabs.Content value="repos">
          <RepoTab projectId={projectId} />
        </Tabs.Content>
        <Tabs.Content value="credentials">
          <CredentialsTab projectId={projectId} />
        </Tabs.Content>
      </Tabs.Root>
    </div>
  )
}
