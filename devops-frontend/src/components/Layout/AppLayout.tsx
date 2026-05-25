import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import * as DropdownMenu from '@radix-ui/react-dropdown-menu'
import {
  LayoutDashboard,
  FolderKanban,
  Code2,
  Rocket,
  ChevronDown,
  LogOut,
  User,
  Zap,
  Workflow,
  PanelLeftClose,
  PanelLeftOpen,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/stores/authStore'

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/projects', label: 'Projects', icon: FolderKanban },
  { to: '/repositories', label: 'Repositories', icon: Code2 },
  { to: '/releases/versions', label: 'Releases', icon: Rocket },
  { to: '/pipelines', label: 'Pipelines', icon: Workflow },
]

export function AppLayout() {
  const navigate = useNavigate()
  const { nickname, username, logout } = useAuthStore()
  const [isCollapsed, setIsCollapsed] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen bg-[var(--color-surface-2)] overflow-hidden">
      {/* Sidebar */}
      <aside
        className={cn(
          'flex-shrink-0 flex flex-col bg-[var(--color-surface)] border-r border-[var(--color-border)] transition-[width] duration-200',
          isCollapsed ? 'w-18' : 'w-56',
        )}
      >
        {/* Logo */}
        <div className={cn('h-14 flex items-center border-b border-[var(--color-border)]', isCollapsed ? 'px-3 justify-center' : 'px-4')}>
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="w-7 h-7 rounded-[var(--radius-sm)] bg-[var(--color-primary)] flex items-center justify-center flex-shrink-0">
              <Zap size={14} className="text-white" />
            </div>
            {!isCollapsed && <span className="font-semibold text-sm text-[var(--color-text)]">DevOps Platform</span>}
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-2 py-3 space-y-0.5 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    'flex items-center rounded-[var(--radius-md)] text-sm transition-colors',
                    isCollapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2',
                    isActive
                      ? 'bg-[var(--color-primary-light)] text-[var(--color-primary)] font-medium'
                      : 'text-[var(--color-text-muted)] hover:bg-[var(--color-surface-3)] hover:text-[var(--color-text)]',
                  )
                }
                title={isCollapsed ? item.label : undefined}
              >
                <Icon size={16} className="flex-shrink-0" />
                {!isCollapsed && item.label}
              </NavLink>
            )
          })}
        </nav>

        <div className="px-2 py-2 border-t border-[var(--color-border)]">
          <button
            type="button"
            className={cn(
              'w-full rounded-[var(--radius-md)] text-[var(--color-text-muted)] hover:bg-[var(--color-surface-3)] hover:text-[var(--color-text)] transition-colors',
              isCollapsed ? 'flex justify-center px-0 py-2.5' : 'flex items-center gap-3 px-3 py-2',
            )}
            onClick={() => setIsCollapsed((current) => !current)}
            aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
            title={isCollapsed ? 'Expand sidebar' : undefined}
          >
            {isCollapsed ? <PanelLeftOpen size={16} /> : <PanelLeftClose size={16} />}
            {!isCollapsed && <span className="text-sm">Collapse Sidebar</span>}
          </button>
        </div>

        {/* User menu */}
        <div className="px-2 py-3 border-t border-[var(--color-border)]">
          <DropdownMenu.Root>
            <DropdownMenu.Trigger asChild>
              <button
                className={cn(
                  'w-full rounded-[var(--radius-md)] hover:bg-[var(--color-surface-3)] transition-colors text-left',
                  isCollapsed ? 'flex justify-center px-0 py-2' : 'flex items-center gap-2.5 px-3 py-2',
                )}
              >
                <div className="w-7 h-7 rounded-full bg-[var(--color-primary-light)] flex items-center justify-center text-[var(--color-primary)] text-xs font-semibold flex-shrink-0">
                  {(nickname ?? username ?? 'U')[0].toUpperCase()}
                </div>
                {!isCollapsed && (
                  <>
                    <div className="flex-1 min-w-0">
                      <div className="text-xs font-medium text-[var(--color-text)] truncate">
                        {nickname ?? username}
                      </div>
                      <div className="text-xs text-[var(--color-text-subtle)] truncate">@{username}</div>
                    </div>
                    <ChevronDown size={12} className="text-[var(--color-text-subtle)]" />
                  </>
                )}
              </button>
            </DropdownMenu.Trigger>
            <DropdownMenu.Portal>
              <DropdownMenu.Content
                side="top"
                align="start"
                sideOffset={4}
                className="w-48 bg-[var(--color-surface)] border border-[var(--color-border)] rounded-[var(--radius-lg)] shadow-lg p-1 z-50"
              >
                <DropdownMenu.Item
                  className="flex items-center gap-2 px-3 py-2 text-sm text-[var(--color-text-muted)] rounded-[var(--radius-sm)] hover:bg-[var(--color-surface-3)] cursor-default outline-none"
                >
                  <User size={14} />
                  Profile
                </DropdownMenu.Item>
                <DropdownMenu.Separator className="my-1 h-px bg-[var(--color-border)]" />
                <DropdownMenu.Item
                  className="flex items-center gap-2 px-3 py-2 text-sm text-red-500 rounded-[var(--radius-sm)] hover:bg-red-50 cursor-pointer outline-none"
                  onSelect={handleLogout}
                >
                  <LogOut size={14} />
                  Sign out
                </DropdownMenu.Item>
              </DropdownMenu.Content>
            </DropdownMenu.Portal>
          </DropdownMenu.Root>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <div className="flex-1 overflow-y-auto">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
