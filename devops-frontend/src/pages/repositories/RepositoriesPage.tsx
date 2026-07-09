import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useQuery as useProjectsQuery } from '@tanstack/react-query'
import { Plus, Code2, ExternalLink, Pencil, Trash2 } from 'lucide-react'
import { reposApi } from '@/api/code'
import { projectsApi } from '@/api/work'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/Dialog'
import type { Repository, RepositoryCreateRequest, RepositoryUpdateRequest, RepositoryVisibility } from '@/types'

const VISIBILITY_STYLES: Record<string, string> = {
  PRIVATE: 'bg-gray-100 text-gray-700',
  PUBLIC: 'bg-emerald-50 text-emerald-700',
  INTERNAL: 'bg-blue-50 text-blue-700',
}

const STATUS_STYLES: Record<string, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-700',
  ARCHIVED: 'bg-amber-50 text-amber-700',
  DISABLED: 'bg-gray-100 text-gray-700',
}

const defaultForm: Omit<RepositoryCreateRequest, 'projectId'> & { projectId: string } = {
  projectId: '',
  namespace: '',
  name: '',
  path: '',
  visibility: 'PUBLIC',
  description: '',
}

export function RepositoriesPage() {
  const qc = useQueryClient()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Repository | null>(null)
  const [filter, setFilter] = useState('')
  const [form, setForm] = useState(defaultForm)
  const [editForm, setEditForm] = useState<RepositoryUpdateRequest>({
    name: '',
    description: '',
    visibility: 'PUBLIC',
  })
  const [formError, setFormError] = useState('')

  const { data: repos = [], isLoading } = useQuery({
    queryKey: ['repositories'],
    queryFn: () => reposApi.list(),
    select: (d) => d ?? [],
  })

  const { data: projects = [] } = useProjectsQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (d) => d ?? [],
  })

  const createMutation = useMutation({
    mutationFn: (data: RepositoryCreateRequest) => reposApi.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['repositories'] })
      setOpen(false)
      setForm(defaultForm)
    },
    onError: (err: unknown) => {
      const msg =
        (err as Error).message ??
        'Failed to create repository.'
      setFormError(msg)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: reposApi.remove,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['repositories'] }),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: RepositoryUpdateRequest }) => reposApi.update(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['repositories'] })
      setEditing(null)
    },
  })

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault()
    setFormError('')
    if (!form.projectId) { setFormError('Please select a project.'); return }
    createMutation.mutate({ ...form, projectId: Number(form.projectId) })
  }

  const filtered = repos.filter(
    (r) =>
      !filter ||
      r.name.toLowerCase().includes(filter.toLowerCase()) ||
      r.path.toLowerCase().includes(filter.toLowerCase()) ||
      r.cloneHttpUrl.toLowerCase().includes(filter.toLowerCase()),
  )

  return (
    <div className="p-8 max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-[var(--color-text)]">Repositories</h1>
          <p className="text-sm text-[var(--color-text-muted)] mt-0.5">{repos.length} repositories linked</p>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus size={15} />
              Link Repository
            </Button>
          </DialogTrigger>
          <DialogContent title="Link Repository">
            <form onSubmit={handleCreate} className="space-y-4">
              <Select
                label="Project"
                value={form.projectId}
                onChange={(e) => setForm((f) => ({ ...f, projectId: e.target.value }))}
                options={[
                  { value: '', label: 'Select a project...' },
                  ...projects.map((p) => ({ value: String(p.id), label: p.name })),
                ]}
              />
              <Input
                label="命名空间"
                value={form.namespace}
                onChange={(e) => setForm((f) => ({ ...f, namespace: e.target.value }))}
                required
              />
              <Input
                label="仓库名称"
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                required
              />
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="仓库路径"
                  value={form.path}
                  onChange={(e) => setForm((f) => ({ ...f, path: e.target.value }))}
                  placeholder="repo-path"
                  required
                />
                <Select
                  label="可见性"
                  value={form.visibility ?? 'PUBLIC'}
                  onChange={(e) => setForm((f) => ({ ...f, visibility: e.target.value as RepositoryVisibility }))}
                  options={[
                    { value: 'PUBLIC', label: '公开' },
                    { value: 'PRIVATE', label: '私有' },
                    { value: 'INTERNAL', label: '内部' },
                  ]}
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-[var(--color-text)]">描述</label>
                <textarea
                  value={form.description ?? ''}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                  rows={3}
                  className="w-full resize-none rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm text-[var(--color-text)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]"
                />
              </div>
              {formError && (
                <p className="text-sm text-[var(--color-danger)] bg-red-50 px-3 py-2 rounded-[var(--radius-md)]">
                  {formError}
                </p>
              )}
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="secondary" type="button" onClick={() => setOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" loading={createMutation.isPending}>
                  Link
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>

        <Dialog open={editing !== null} onOpenChange={(value) => !value && setEditing(null)}>
          <DialogContent title="编辑仓库">
            <form
              onSubmit={(e) => {
                e.preventDefault()
                if (!editing) return
                updateMutation.mutate({ id: editing.id, data: editForm })
              }}
              className="space-y-4"
            >
              <Input label="命名空间" value={editing?.namespace ?? ''} disabled />
              <Input label="仓库路径" value={editing?.path ?? ''} disabled />
              <Input
                label="仓库名称"
                value={editForm.name ?? ''}
                onChange={(e) => setEditForm((current) => ({ ...current, name: e.target.value }))}
                required
              />
              <Select
                label="可见性"
                value={editForm.visibility ?? 'PUBLIC'}
                onChange={(e) =>
                  setEditForm((current) => ({ ...current, visibility: e.target.value as RepositoryVisibility }))
                }
                options={[
                  { value: 'PUBLIC', label: '公开' },
                  { value: 'PRIVATE', label: '私有' },
                  { value: 'INTERNAL', label: '内部' },
                ]}
              />
              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-[var(--color-text)]">描述</label>
                <textarea
                  value={editForm.description ?? ''}
                  onChange={(e) => setEditForm((current) => ({ ...current, description: e.target.value }))}
                  rows={3}
                  className="w-full resize-none rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm text-[var(--color-text)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="secondary" type="button" onClick={() => setEditing(null)}>
                  取消
                </Button>
                <Button type="submit" loading={updateMutation.isPending}>
                  保存
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {/* Search */}
      <div className="mb-4">
        <Input
          placeholder="Search repositories..."
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="max-w-xs"
        />
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-20 bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)] animate-pulse" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-center">
          <Code2 size={40} className="text-[var(--color-border)] mb-3" />
          <p className="text-[var(--color-text-muted)] font-medium">No repositories found</p>
          <p className="text-sm text-[var(--color-text-subtle)] mt-1">Link a repository to a project to get started.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((r) => {
            const project = projects.find((p) => p.id === r.projectId)
            return (
              <div
                key={r.id}
                className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)] p-5 flex items-center gap-4"
              >
                <div className="w-9 h-9 rounded-[var(--radius-md)] bg-[var(--color-surface-3)] flex items-center justify-center flex-shrink-0">
                  <Code2 size={16} className="text-[var(--color-text-muted)]" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-0.5">
                    <p className="text-sm font-semibold text-[var(--color-text)]">{r.name}</p>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${VISIBILITY_STYLES[r.visibility] ?? 'bg-gray-100 text-gray-700'}`}>
                      {r.visibilityDescription}
                    </span>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_STYLES[r.status] ?? 'bg-gray-100 text-gray-700'}`}>
                      {r.statusDescription}
                    </span>
                  </div>
                  <a
                    href={r.cloneHttpUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-xs text-[var(--color-primary)] hover:underline flex items-center gap-1"
                  >
                    {r.cloneHttpUrl}
                    <ExternalLink size={10} />
                  </a>
                </div>
                <div className="flex items-center gap-3">
                  {project && (
                    <span className="text-xs text-[var(--color-text-muted)] bg-[var(--color-surface-3)] px-2 py-1 rounded-[var(--radius-sm)]">
                      {project.name}
                    </span>
                  )}
                  <span className="text-xs font-mono text-[var(--color-text-subtle)]">{r.namespace}/{r.path}</span>
                  <button
                    className="p-1.5 rounded-[var(--radius-sm)] hover:bg-[var(--color-surface-3)] text-[var(--color-text-subtle)] transition-colors"
                    onClick={() => {
                      setEditForm({
                        name: r.name,
                        description: r.description ?? '',
                        visibility: r.visibility,
                      })
                      setEditing(r)
                    }}
                  >
                    <Pencil size={13} />
                  </button>
                  <button
                    className="p-1.5 rounded-[var(--radius-sm)] hover:bg-red-50 text-[var(--color-text-subtle)] hover:text-red-500 transition-colors"
                    onClick={() => {
                      if (confirm(`Remove repository "${r.name}"?`)) deleteMutation.mutate(r.id)
                    }}
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
