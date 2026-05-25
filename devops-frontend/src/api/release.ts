import { apiGet, apiPost, apiPut, apiPatch, apiDelete } from './client'
import type {
  PipelineRun,
  PipelineRunCreateRequest,
  PipelineRunQueryRequest,
  PipelineRunStatusUpdateRequest,
  Version,
  VersionCreateRequest,
  VersionUpdateRequest,
  VersionStatusUpdateRequest,
} from '@/types'

export const releasesApi = {
  list: (projectId?: number) =>
    apiGet<Version[]>('/release', projectId ? { projectId } : undefined),
  get: (id: number) => apiGet<Version>(`/release/${id}`),
  create: (data: VersionCreateRequest) => apiPost<Version>('/release', data),
  update: (id: number, data: VersionUpdateRequest) => apiPut<Version>(`/release/${id}`, data),
  updateStatus: (id: number, data: VersionStatusUpdateRequest) =>
    apiPatch<Version>(`/release/${id}/status`, data),
  remove: (id: number) => apiDelete(`/release/${id}`),

  listPipelineRuns: (params?: PipelineRunQueryRequest) =>
    apiGet<PipelineRun[]>('/release/pipeline-runs', params),
  getPipelineRun: (id: number) => apiGet<PipelineRun>(`/release/pipeline-runs/${id}`),
  createPipelineRun: (data: PipelineRunCreateRequest) =>
    apiPost<PipelineRun>('/release/pipeline-runs', data),
  updatePipelineRunStatus: (id: number, data: PipelineRunStatusUpdateRequest) =>
    apiPatch<PipelineRun>(`/release/pipeline-runs/${id}/status`, data),
  removePipelineRun: (id: number) => apiDelete(`/release/pipeline-runs/${id}`),
}
