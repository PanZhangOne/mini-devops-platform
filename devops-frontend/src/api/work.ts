import { apiGet, apiPost, apiPut, apiDelete } from './client'
import type {
  Project,
  ProjectModule,
  ProjectModuleCreateRequest,
  ProjectModuleTree,
  ProjectModuleUpdateRequest,
  ProjectCreateRequest,
  ProjectUpdateRequest,
  ProjectTaskStatsVO,
  Task,
  TaskComment,
  TaskCommentCreateRequest,
  TaskCommentTree,
  TaskCreateRequest,
  TaskDetail,
  TaskProperty,
  TaskPropertyCreateRequest,
  TaskPropertyUpdateRequest,
  TaskPropertyValue,
  TaskPropertyValueSaveRequest,
  TaskUpdateRequest,
  TaskStatusChangeRequest,
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
  detail: (id: number) => apiGet<TaskDetail>(`/work/tasks/${id}/detail`),
  create: (data: TaskCreateRequest) => apiPost<Task>('/work/tasks', data),
  update: (id: number, data: TaskUpdateRequest) => apiPut<Task>(`/work/tasks/${id}`, data),
  updateStatus: (id: number, data: TaskStatusChangeRequest) =>
    apiPost<void>(`/work/tasks/${id}/status`, data),
  remove: (id: number) => apiDelete(`/work/tasks/${id}`),
}

export const taskPropertiesApi = {
  list: (projectId: number) => apiGet<TaskProperty[]>(`/work/projects/${projectId}/task-properties`),
  get: (id: number) => apiGet<TaskProperty>(`/work/task-properties/${id}`),
  create: (projectId: number, data: TaskPropertyCreateRequest) =>
    apiPost<TaskProperty>(`/work/projects/${projectId}/task-properties`, data),
  update: (id: number, data: TaskPropertyUpdateRequest) =>
    apiPut<TaskProperty>(`/work/task-properties/${id}`, data),
  remove: (id: number) => apiDelete(`/work/task-properties/${id}`),
}

export const taskPropertyValuesApi = {
  list: (taskId: number) => apiGet<TaskPropertyValue[]>(`/work/tasks/${taskId}/property-values`),
  save: (taskId: number, data: TaskPropertyValueSaveRequest) =>
    apiPut<void>(`/work/tasks/${taskId}/property-values`, data),
}

export const taskCommentsApi = {
  list: (taskId: number) => apiGet<TaskCommentTree[]>(`/work/tasks/${taskId}/comments`),
  create: (taskId: number, data: TaskCommentCreateRequest) =>
    apiPost<TaskComment>(`/work/tasks/${taskId}/comments`, data),
  remove: (id: number) => apiDelete(`/work/task-comments/${id}`),
}

export const projectModulesApi = {
  list: (projectId: number) => apiGet<ProjectModule[]>(`/work/projects/${projectId}/modules`),
  tree: (projectId: number) => apiGet<ProjectModuleTree[]>(`/work/projects/${projectId}/modules/tree`),
  get: (id: number) => apiGet<ProjectModule>(`/work/project-modules/${id}`),
  create: (projectId: number, data: ProjectModuleCreateRequest) =>
    apiPost<ProjectModule>(`/work/projects/${projectId}/modules`, data),
  update: (id: number, data: ProjectModuleUpdateRequest) =>
    apiPut<ProjectModule>(`/work/project-modules/${id}`, data),
  remove: (id: number) => apiDelete(`/work/project-modules/${id}`),
}
