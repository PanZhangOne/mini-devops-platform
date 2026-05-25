import { apiGet, apiPost, apiPut, apiPatch, apiDelete } from './client'
import type {
  Project,
  ProjectCreateRequest,
  ProjectUpdateRequest,
  ProjectTaskStatsVO,
  Task,
  TaskCreateRequest,
  TaskUpdateRequest,
  TaskStatusUpdateRequest,
} from '@/types'

// ─── Projects ─────────────────────────────────────────────────────────────────

export const projectsApi = {
  list: () => apiGet<Project[]>('/work/projects'),
  get: (id: number) => apiGet<Project>(`/work/projects/${id}`),
  create: (data: ProjectCreateRequest) => apiPost<Project>('/work/projects', data),
  update: (id: number, data: ProjectUpdateRequest) => apiPut<Project>(`/work/projects/${id}`, data),
  remove: (id: number) => apiDelete(`/work/projects/${id}`),
  taskStats: (projectId: number) =>
    apiGet<ProjectTaskStatsVO>(`/work/projects/${projectId}/task-stats`),
  tasks: (projectId: number) => apiGet<Task[]>(`/work/projects/${projectId}/tasks`),
}

// ─── Tasks ────────────────────────────────────────────────────────────────────

export const tasksApi = {
  list: () => apiGet<Task[]>('/work/tasks'),
  get: (id: number) => apiGet<Task>(`/work/tasks/${id}`),
  create: (data: TaskCreateRequest) => apiPost<Task>('/work/tasks', data),
  update: (id: number, data: TaskUpdateRequest) => apiPut<Task>(`/work/tasks/${id}`, data),
  updateStatus: (id: number, data: TaskStatusUpdateRequest) =>
    apiPatch<Task>(`/work/tasks/${id}/status`, data),
  remove: (id: number) => apiDelete(`/work/tasks/${id}`),
}
