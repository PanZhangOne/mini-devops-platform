import { apiGet, apiPost, apiPut, apiDelete } from './client'
import type { Repository, RepositoryCreateRequest, RepositoryUpdateRequest } from '@/types'

interface PaginatedResponse<T> {
  current: number
  size: number
  total: number
  records: T[]
}

export const reposApi = {
  list: async (projectId?: number) => {
    const data = await apiGet<PaginatedResponse<Repository>>(
      '/code/repositories',
      projectId ? { projectId } : undefined,
    )
    return data.records ?? []
  },
  get: (id: number) => apiGet<Repository>(`/code/repositories/${id}`),
  create: (data: RepositoryCreateRequest) => apiPost<Repository>('/code/repositories', data),
  update: (id: number, data: RepositoryUpdateRequest) =>
    apiPut<Repository>(`/code/repositories/${id}`, data),
  remove: (id: number) => apiDelete(`/code/repositories/${id}`),
}
