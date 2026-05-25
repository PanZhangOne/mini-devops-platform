import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import * as Tabs from '@radix-ui/react-tabs'
import { ArrowLeft, Trash2, Plus, Flag } from 'lucide-react'
import { projectsApi, tasksApi } from '@/api/work'
import { reposApi } from '@/api/code'
import { Button } from '@/components/ui/Button'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/Dialog'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import type { Task, TaskCreateRequest, TaskPriority, TaskStatus } from '@/types'

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

function TaskCard({ task, onStatusChange }: { task: Task; onStatusChange: (id: number, status: TaskStatus) => void }) {
  const nextStatus: Partial<Record<TaskStatus, TaskStatus>> = {
    TODO: 'IN_PROGRESS',
    IN_PROGRESS: 'TESTING',
    TESTING: 'DONE',
  }
  return (
    <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-3.5 shadow-sm hover:shadow-md transition-shadow">
      <p className="text-sm font-medium text-[var(--color-text)] mb-2 leading-snug">{task.title}</p>
      {task.description && (
        <p className="text-xs text-[var(--color-text-muted)] mb-3 line-clamp-2">{task.description}</p>
      )}
      <div className="flex items-center justify-between">
        <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${PRIORITY_STYLES[task.priority]}`}>
          <Flag size={10} className="inline mr-1" />
          {task.priorityDescription}
        </span>
        {nextStatus[task.status] && (
          <button
            className="text-xs text-[var(--color-primary)] hover:underline"
            onClick={() => onStatusChange(task.id, nextStatus[task.status]!)}
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
  const [taskForm, setTaskForm] = useState<Omit<TaskCreateRequest, 'projectId'>>({
    title: '',
    description: '',
    priority: 'MEDIUM',
  })

  const { data: tasks = [] } = useQuery({
    queryKey: ['project-tasks', projectId],
    queryFn: () => projectsApi.tasks(projectId),
    select: (d) => d ?? [],
  })

  const createTask = useMutation({
    mutationFn: (data: TaskCreateRequest) => tasksApi.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-tasks', projectId] })
      qc.invalidateQueries({ queryKey: ['project-stats', projectId] })
      setCreateOpen(false)
      setTaskForm({ title: '', description: '', priority: 'MEDIUM' })
    },
  })

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: TaskStatus }) =>
      tasksApi.updateStatus(id, { status }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['project-tasks', projectId] })
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
              New Task
            </Button>
          </DialogTrigger>
          <DialogContent title="Create Task">
            <form
              onSubmit={(e) => {
                e.preventDefault()
                createTask.mutate({ ...taskForm, projectId })
              }}
              className="space-y-4"
            >
              <Input
                label="Title"
                value={taskForm.title}
                onChange={(e) => setTaskForm((f) => ({ ...f, title: e.target.value }))}
                placeholder="Task title"
                required
              />
              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-[var(--color-text)]">Description</label>
                <textarea
                  value={taskForm.description}
                  onChange={(e) => setTaskForm((f) => ({ ...f, description: e.target.value }))}
                  rows={3}
                  className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
                />
              </div>
              <Select
                label="Priority"
                value={taskForm.priority}
                onChange={(e) => setTaskForm((f) => ({ ...f, priority: e.target.value as TaskPriority }))}
                options={[
                  { value: 'LOW', label: 'Low' },
                  { value: 'MEDIUM', label: 'Medium' },
                  { value: 'HIGH', label: 'High' },
                  { value: 'URGENT', label: 'Urgent' },
                ]}
              />
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="secondary" type="button" onClick={() => setCreateOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" loading={createTask.isPending}>
                  Create
                </Button>
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
                  <TaskCard key={task.id} task={task} onStatusChange={handleStatusChange} />
                ))}
              </div>
            </div>
          )
        })}
      </div>
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
            { value: 'repos', label: 'Repositories' },
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
        <Tabs.Content value="repos">
          <RepoTab projectId={projectId} />
        </Tabs.Content>
      </Tabs.Root>
    </div>
  )
}
