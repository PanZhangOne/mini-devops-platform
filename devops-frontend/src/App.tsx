import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ProtectedRoute } from '@/router/ProtectedRoute'
import { AppLayout } from '@/components/Layout/AppLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { RegisterPage } from '@/pages/auth/RegisterPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { ProjectListPage } from '@/pages/projects/ProjectListPage'
import { ProjectDetailPage } from '@/pages/projects/ProjectDetailPage'
import { RepositoriesPage } from '@/pages/repositories/RepositoriesPage'
import { ReleasesPage } from '@/pages/releases/ReleasesPage'
import { PipelinesPage } from '@/pages/pipelines/PipelinesPage'
import { PipelineDetailPage } from '@/pages/pipelines/PipelineDetailPage'
import { PipelineRunDetailPage } from '@/pages/pipelines/PipelineRunDetailPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/projects" element={<ProjectListPage />} />
              <Route path="/projects/:id" element={<ProjectDetailPage />} />
              <Route path="/repositories" element={<RepositoriesPage />} />
              <Route path="/pipelines" element={<PipelinesPage />} />
              <Route path="/pipelines/runs/:runId" element={<PipelineRunDetailPage />} />
              <Route path="/pipelines/:id" element={<PipelineDetailPage />} />
              <Route path="/releases" element={<Navigate to="/releases/versions" replace />} />
              <Route path="/releases/versions" element={<ReleasesPage defaultTab="versions" />} />
              <Route path="/releases/pipelines" element={<Navigate to="/pipelines" replace />} />
            </Route>
          </Route>

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
