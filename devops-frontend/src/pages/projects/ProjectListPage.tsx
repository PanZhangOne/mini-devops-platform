import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Plus, FolderKanban, ChevronRight } from 'lucide-react'
import { projectsApi } from '@/api/work'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/Dialog'
import type { Project, ProjectCreateRequest } from '@/types'

const STATUS_STYLES: Record<Project['status'], string> = {
  PLANNING: 'bg-slate-100 text-slate-600',
  DEVELOPING: 'bg-blue-50 text-blue-700',
  TESTING: 'bg-amber-50 text-amber-700',
  RELEASED: 'bg-emerald-50 text-emerald-700',
  ARCHIVED: 'bg-gray-100 text-gray-500',
}

export function ProjectListPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState<ProjectCreateRequest>({ name: '', code: '', description: '' })
  const [formError, setFormError] = useState('')

  const { data: projects = [], isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (d) => d ?? [],
  })

  const createMutation = useMutation({
    mutationFn: projectsApi.create,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['projects'] })
      setOpen(false)
      setForm({ name: '', code: '', description: '' })
    },
    onError: (err: unknown) => {
      const msg =
        (err as Error).message ??
        'Failed to create project.'
      setFormError(msg)
    },
  })

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault()
    setFormError('')
    createMutation.mutate(form)
  }

  return (
    <div className="p-8 max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-[var(--color-text)]">Projects</h1>
          <p className="text-sm text-[var(--color-text-muted)] mt-0.5">{projects.length} projects total</p>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus size={15} />
              New Project
            </Button>
          </DialogTrigger>
          <DialogContent title="Create Project">
            <form onSubmit={handleCreate} className="space-y-4">
              <Input
                label="Project Name"
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                placeholder="My Awesome Project"
                required
              />
              <Input
                label="Project Code"
                value={form.code}
                onChange={(e) => setForm((f) => ({ ...f, code: e.target.value.toUpperCase() }))}
                placeholder="MAP"
                required
              />
              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-[var(--color-text)]">Description</label>
                <textarea
                  value={form.description}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                  placeholder="Optional description..."
                  rows={3}
                  className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] placeholder:text-[var(--color-text-subtle)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-1 focus:ring-[var(--color-primary)] resize-none"
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
                  Create
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)] p-5 h-32 animate-pulse" />
          ))}
        </div>
      ) : projects.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-center">
          <FolderKanban size={40} className="text-[var(--color-border)] mb-3" />
          <p className="text-[var(--color-text-muted)] font-medium">No projects yet</p>
          <p className="text-sm text-[var(--color-text-subtle)] mt-1">Create your first project to get started.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {projects.map((p) => (
            <button
              key={p.id}
              onClick={() => navigate(`/projects/${p.id}`)}
              className="group bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)] p-5 text-left hover:border-[var(--color-primary)] hover:shadow-md transition-all"
            >
              <div className="flex items-start justify-between mb-3">
                <div className="w-9 h-9 rounded-[var(--radius-md)] bg-[var(--color-primary-light)] flex items-center justify-center">
                  <FolderKanban size={16} className="text-[var(--color-primary)]" />
                </div>
                <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${STATUS_STYLES[p.status]}`}>
                  {p.statusDescription}
                </span>
              </div>
              <p className="font-semibold text-sm text-[var(--color-text)] mb-1">{p.name}</p>
              <p className="text-xs text-[var(--color-text-muted)] mb-3 line-clamp-2">
                {p.description || p.code}
              </p>
              <div className="flex items-center justify-between">
                <span className="text-xs text-[var(--color-text-subtle)] font-mono">{p.code}</span>
                <ChevronRight
                  size={14}
                  className="text-[var(--color-text-subtle)] group-hover:text-[var(--color-primary)] transition-colors"
                />
              </div>
            </button>
          ))}
        </div>
      )}

      {/* Status filter pills (visual only for now) */}
      {projects.length > 0 && (
        <div className="mt-6 flex flex-wrap gap-2">
          {(['PLANNING', 'DEVELOPING', 'TESTING', 'RELEASED', 'ARCHIVED'] as const).map((s) => {
            const count = projects.filter((p) => p.status === s).length
            return count > 0 ? (
              <span key={s} className={`text-xs px-2.5 py-1 rounded-full font-medium ${STATUS_STYLES[s]}`}>
                {s.charAt(0) + s.slice(1).toLowerCase()} ({count})
              </span>
            ) : null
          })}
        </div>
      )}
    </div>
  )
}

// Re-export for convenience
export { Select }
