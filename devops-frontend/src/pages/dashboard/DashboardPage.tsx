import { useQuery } from '@tanstack/react-query'
import { FolderKanban, CheckSquare, Code2, Rocket, TrendingUp } from 'lucide-react'
import { projectsApi } from '@/api/work'
import { reposApi } from '@/api/code'
import { useAuthStore } from '@/stores/authStore'
import type { Project } from '@/types'

function StatCard({
  label,
  value,
  icon: Icon,
  color,
}: {
  label: string
  value: number | string
  icon: React.ElementType
  color: string
}) {
  return (
    <div className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)] p-5">
      <div className="flex items-center justify-between mb-3">
        <p className="text-sm text-[var(--color-text-muted)]">{label}</p>
        <div className={`w-8 h-8 rounded-[var(--radius-md)] flex items-center justify-center ${color}`}>
          <Icon size={16} />
        </div>
      </div>
      <p className="text-2xl font-semibold text-[var(--color-text)]">{value}</p>
    </div>
  )
}

function ProjectStatusBadge({ status }: { status: Project['status'] }) {
  const styles: Record<Project['status'], string> = {
    PLANNING: 'bg-slate-100 text-slate-600',
    DEVELOPING: 'bg-blue-50 text-blue-700',
    TESTING: 'bg-amber-50 text-amber-700',
    RELEASED: 'bg-emerald-50 text-emerald-700',
    ARCHIVED: 'bg-gray-100 text-gray-500',
  }
  const labels: Record<Project['status'], string> = {
    PLANNING: 'Planning',
    DEVELOPING: 'Developing',
    TESTING: 'Testing',
    RELEASED: 'Released',
    ARCHIVED: 'Archived',
  }
  return (
    <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${styles[status]}`}>
      {labels[status]}
    </span>
  )
}

export function DashboardPage() {
  const nickname = useAuthStore((s) => s.nickname)
  const username = useAuthStore((s) => s.username)

  const { data: projects = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: projectsApi.list,
    select: (d) => d ?? [],
  })

  const { data: repos = [] } = useQuery({
    queryKey: ['repositories'],
    queryFn: () => reposApi.list(),
    select: (d) => d ?? [],
  })

  const activeProjects = projects.filter((p) =>
    ['DEVELOPING', 'TESTING'].includes(p.status),
  ).length

  const recentProjects = [...projects]
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 5)

  return (
    <div className="p-8 max-w-6xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-semibold text-[var(--color-text)]">
          Welcome back, {nickname ?? username} 👋
        </h1>
        <p className="text-sm text-[var(--color-text-muted)] mt-1">
          Here&apos;s an overview of your DevOps platform.
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-8">
        <StatCard
          label="Total Projects"
          value={projects.length}
          icon={FolderKanban}
          color="bg-[var(--color-primary-light)] text-[var(--color-primary)]"
        />
        <StatCard
          label="Active Projects"
          value={activeProjects}
          icon={TrendingUp}
          color="bg-emerald-50 text-emerald-600"
        />
        <StatCard
          label="Repositories"
          value={repos.length}
          icon={Code2}
          color="bg-blue-50 text-blue-600"
        />
        <StatCard
          label="Releases"
          value="—"
          icon={Rocket}
          color="bg-amber-50 text-amber-600"
        />
      </div>

      {/* Recent Projects */}
      <div className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] border border-[var(--color-border)]">
        <div className="px-6 py-4 border-b border-[var(--color-border)] flex items-center gap-2">
          <CheckSquare size={16} className="text-[var(--color-text-muted)]" />
          <h2 className="text-sm font-semibold text-[var(--color-text)]">Recent Projects</h2>
        </div>
        {recentProjects.length === 0 ? (
          <div className="p-12 text-center text-[var(--color-text-muted)] text-sm">
            No projects yet. Create your first project.
          </div>
        ) : (
          <div className="divide-y divide-[var(--color-border)]">
            {recentProjects.map((p) => (
              <div key={p.id} className="px-6 py-4 flex items-center justify-between hover:bg-[var(--color-surface-2)] transition-colors">
                <div>
                  <p className="text-sm font-medium text-[var(--color-text)]">{p.name}</p>
                  <p className="text-xs text-[var(--color-text-muted)] mt-0.5">{p.code}</p>
                </div>
                <ProjectStatusBadge status={p.status} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
