import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useQuery as useProjectsQuery } from '@tanstack/react-query'
import { Plus, Code2, ExternalLink, Trash2 } from 'lucide-react'
import { reposApi } from '@/api/code'
import { projectsApi } from '@/api/work'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/Dialog'
import type { RepositoryCreateRequest, RepoType } from '@/types'

const REPO_TYPE_STYLES: Record<string, string> = {
  GITHUB: 'bg-gray-100 text-gray-700',
  GITLAB: 'bg-orange-50 text-orange-700',
  GITEE: 'bg-red-50 text-red-700',
  CUSTOM: 'bg-purple-50 text-purple-700',
}

const defaultForm: Omit<RepositoryCreateRequest, 'projectId'> & { projectId: string } = {
  projectId: '',
  repoName: '',
  repoUrl: '',
  defaultBranch: 'main',
  repoType: 'GITHUB',
  description: '',
}

export function RepositoriesPage() {
  const qc = useQueryClient()
  const [open, setOpen] = useState(false)
  const [filter, setFilter] = useState('')
  const [form, setForm] = useState(defaultForm)
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

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault()
    setFormError('')
    if (!form.projectId) { setFormError('Please select a project.'); return }
    createMutation.mutate({ ...form, projectId: Number(form.projectId) })
  }

  const filtered = repos.filter(
    (r) =>
      !filter ||
      r.repoName.toLowerCase().includes(filter.toLowerCase()) ||
      r.repoUrl.toLowerCase().includes(filter.toLowerCase()),
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
                label="Repository Name"
                value={form.repoName}
                onChange={(e) => setForm((f) => ({ ...f, repoName: e.target.value }))}
                required
              />
              <Input
                label="Repository URL"
                value={form.repoUrl}
                onChange={(e) => setForm((f) => ({ ...f, repoUrl: e.target.value }))}
                placeholder="https://github.com/org/repo"
                required
              />
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Default Branch"
                  value={form.defaultBranch}
                  onChange={(e) => setForm((f) => ({ ...f, defaultBranch: e.target.value }))}
                />
                <Select
                  label="Type"
                  value={form.repoType}
                  onChange={(e) => setForm((f) => ({ ...f, repoType: e.target.value as RepoType }))}
                  options={[
                    { value: 'GITHUB', label: 'GitHub' },
                    { value: 'GITLAB', label: 'GitLab' },
                    { value: 'GITEE', label: 'Gitee' },
                    { value: 'CUSTOM', label: 'Custom' },
                  ]}
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
                    <p className="text-sm font-semibold text-[var(--color-text)]">{r.repoName}</p>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${REPO_TYPE_STYLES[r.repoType] ?? 'bg-gray-100 text-gray-700'}`}>
                      {r.repoTypeDescription}
                    </span>
                  </div>
                  <a
                    href={r.repoUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-xs text-[var(--color-primary)] hover:underline flex items-center gap-1"
                  >
                    {r.repoUrl}
                    <ExternalLink size={10} />
                  </a>
                </div>
                <div className="flex items-center gap-3">
                  {project && (
                    <span className="text-xs text-[var(--color-text-muted)] bg-[var(--color-surface-3)] px-2 py-1 rounded-[var(--radius-sm)]">
                      {project.name}
                    </span>
                  )}
                  <span className="text-xs font-mono text-[var(--color-text-subtle)]">/{r.defaultBranch}</span>
                  <button
                    className="p-1.5 rounded-[var(--radius-sm)] hover:bg-red-50 text-[var(--color-text-subtle)] hover:text-red-500 transition-colors"
                    onClick={() => {
                      if (confirm(`Remove repository "${r.repoName}"?`)) deleteMutation.mutate(r.id)
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
