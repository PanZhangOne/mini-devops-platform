import { apiDelete, apiGet, apiPatch, apiPost, apiPut } from './client'
import type {
  PipelineDefinition,
  PipelineDefinitionCreateRequest,
  PipelineDefinitionUpdateRequest,
  PipelineLogRecord,
  PipelineRunRecord,
  PipelineRunRecordCreateRequest,
  PipelineRunRecordQueryRequest,
  PipelineStepDefinition,
  PipelineStepDefinitionCreateRequest,
  PipelineStepDefinitionUpdateRequest,
  PipelineStepRunRecord,
  RunnerRecord,
  RunnerStatusUpdatePayload,
} from '@/types'

const PIPELINE_BASE = '/pipeline'

export const pipelineApi = {
  listPipelines: (projectId?: number) =>
    apiGet<PipelineDefinition[]>(`${PIPELINE_BASE}/pipelines`, projectId ? { projectId } : undefined),
  getPipeline: (id: number) => apiGet<PipelineDefinition>(`${PIPELINE_BASE}/pipelines/${id}`),
  createPipeline: (data: PipelineDefinitionCreateRequest) =>
    apiPost<PipelineDefinition>(`${PIPELINE_BASE}/pipelines`, data),
  updatePipeline: (id: number, data: PipelineDefinitionUpdateRequest) =>
    apiPut<PipelineDefinition>(`${PIPELINE_BASE}/pipelines/${id}`, data),
  removePipeline: (id: number) => apiDelete(`${PIPELINE_BASE}/pipelines/${id}`),

  listPipelineSteps: (pipelineId: number) =>
    apiGet<PipelineStepDefinition[]>(`${PIPELINE_BASE}/pipelines/${pipelineId}/steps`),
  createPipelineStep: (pipelineId: number, data: PipelineStepDefinitionCreateRequest) =>
    apiPost<PipelineStepDefinition>(`${PIPELINE_BASE}/pipelines/${pipelineId}/steps`, data),
  updatePipelineStep: (id: number, data: PipelineStepDefinitionUpdateRequest) =>
    apiPut<PipelineStepDefinition>(`${PIPELINE_BASE}/pipeline-steps/${id}`, data),
  removePipelineStep: (id: number) => apiDelete(`${PIPELINE_BASE}/pipeline-steps/${id}`),

  listPipelineRuns: (params?: PipelineRunRecordQueryRequest) =>
    apiGet<PipelineRunRecord[]>(`${PIPELINE_BASE}/pipeline-runs`, params),
  getPipelineRun: (id: number) => apiGet<PipelineRunRecord>(`${PIPELINE_BASE}/pipeline-runs/${id}`),
  createPipelineRun: (pipelineId: number, data: PipelineRunRecordCreateRequest) =>
    apiPost<PipelineRunRecord>(`${PIPELINE_BASE}/pipelines/${pipelineId}/runs`, data),
  listPipelineRunSteps: (pipelineRunId: number) =>
    apiGet<PipelineStepRunRecord[]>(`${PIPELINE_BASE}/pipeline-runs/${pipelineRunId}/steps`),
  listPipelineRunLogs: (pipelineRunId: number) =>
    apiGet<PipelineLogRecord[]>(`${PIPELINE_BASE}/pipeline-runs/${pipelineRunId}/logs`),

  listRunners: () => apiGet<RunnerRecord[]>(`${PIPELINE_BASE}/runners`),
  updateRunnerStatus: (id: number, data: RunnerStatusUpdatePayload) =>
    apiPatch<RunnerRecord>(`${PIPELINE_BASE}/runners/${id}/status`, data),
}