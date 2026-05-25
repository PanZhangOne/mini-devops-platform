import { apiGet, apiPost, apiPut, apiDelete } from './client'
import type { Repository, RepositoryCreateRequest, RepositoryUpdateRequest } from '@/types'

export const reposApi = {
  list: (projectId?: number) =>
    apiGet<Repository[]>('/code/repositories', projectId ? { projectId } : undefined),
  get: (id: number) => apiGet<Repository>(`/code/repositories/${id}`),
  create: (data: RepositoryCreateRequest) => apiPost<Repository>('/code/repositories', data),
  update: (id: number, data: RepositoryUpdateRequest) =>
    apiPut<Repository>(`/code/repositories/${id}`, data),
  remove: (id: number) => apiDelete(`/code/repositories/${id}`),
}
