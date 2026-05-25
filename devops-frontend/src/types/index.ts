// ─── Generic Response ────────────────────────────────────────────────────────

export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
}

export interface LoginVO {
  token: string
  userId: number
  username: string
  nickname: string
}

export interface UserVO {
  id: number
  username: string
  nickname: string
  status: string
  createdAt: string
  updatedAt: string
}

// ─── Project ──────────────────────────────────────────────────────────────────

export type ProjectStatus = 'PLANNING' | 'DEVELOPING' | 'TESTING' | 'RELEASED' | 'ARCHIVED'

export interface Project {
  id: number
  name: string
  code: string
  description: string
  ownerId: number
  status: ProjectStatus
  statusDescription: string
  createdAt: string
  updatedAt: string
}

export interface ProjectCreateRequest {
  name: string
  code: string
  description?: string
  ownerId?: number
}

export interface ProjectUpdateRequest {
  name: string
  code: string
  description?: string
  ownerId?: number
}

export interface ProjectTaskStatsVO {
  totalTasks: number
  todoCount: number
  inProgressCount: number
  testingCount: number
  doneCount: number
  cancelledCount: number
}

// ─── Task ─────────────────────────────────────────────────────────────────────

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'TESTING' | 'DONE' | 'CANCELLED'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

export interface Task {
  id: number
  projectId: number
  title: string
  description: string
  assigneeId: number
  status: TaskStatus
  statusDescription: string
  priority: TaskPriority
  priorityDescription: string
  deadline: string
  createdAt: string
  updatedAt: string
}

export interface TaskCreateRequest {
  projectId: number
  title: string
  description?: string
  assigneeId?: number
  priority: TaskPriority
  deadline?: string
}

export interface TaskUpdateRequest {
  title: string
  description?: string
  assigneeId?: number
  priority: TaskPriority
  deadline?: string
}

export interface TaskStatusUpdateRequest {
  status: TaskStatus
}

// ─── Repository ───────────────────────────────────────────────────────────────

export type RepoType = 'GITLAB' | 'GITHUB' | 'GITEE' | 'CUSTOM'

export interface Repository {
  id: number
  projectId: number
  repoName: string
  repoUrl: string
  defaultBranch: string
  repoType: RepoType
  repoTypeDescription: string
  description: string
  createdAt: string
  updatedAt: string
}

export interface RepositoryCreateRequest {
  projectId: number
  repoName: string
  repoUrl: string
  defaultBranch: string
  repoType: RepoType
  description?: string
}

export interface RepositoryUpdateRequest {
  repoName: string
  repoUrl: string
  defaultBranch: string
  repoType: RepoType
  description?: string
}

// ─── Version (Release) ────────────────────────────────────────────────────────

export type VersionStatus = 'DRAFT' | 'READY' | 'RELEASED' | 'ROLLBACKED' | 'CANCELLED'

export interface Version {
  id: number
  projectId: number
  repositoryId: number
  versionNo: string
  gitTag: string
  branchName: string
  commitHash: string
  title: string
  description: string
  status: VersionStatus
  statusDescription: string
  createdBy: number
  releasedAt: string
  createdAt: string
  updatedAt: string
}

export interface VersionCreateRequest {
  projectId: number
  repositoryId: number
  versionNo: string
  gitTag?: string
  branchName?: string
  commitHash?: string
  title: string
  description?: string
}

export interface VersionUpdateRequest {
  title: string
  gitTag?: string
  branchName?: string
  commitHash?: string
  description?: string
}

export interface VersionStatusUpdateRequest {
  status: VersionStatus
}

export type PipelineEnv = 'DEV' | 'TEST' | 'STAGING' | 'PROD'

export type PipelineRunStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELLED'

export type PipelineTriggerType = 'MANUAL' | 'WEBHOOK' | 'SCHEDULED'

export interface PipelineRun {
  id: number
  projectId: number
  repositoryId: number
  versionId: number
  runNo: string
  env: PipelineEnv
  envDescription: string
  status: PipelineRunStatus
  statusDescription: string
  imageTag: string
  commitHash: string
  triggerUserId: number
  triggerType: PipelineTriggerType
  triggerTypeDescription: string
  startedAt: string
  finishedAt: string
  durationSeconds: number
  logText: string
  errorMessage: string
  createdAt: string
  updatedAt: string
}

export interface PipelineRunCreateRequest {
  projectId: number
  repositoryId: number
  versionId: number
  env: PipelineEnv
  imageTag?: string
  commitHash?: string
  triggerType: PipelineTriggerType
}

export interface PipelineRunQueryRequest {
  projectId?: number
  repositoryId?: number
  versionId?: number
  env?: PipelineEnv
  status?: PipelineRunStatus
}

export interface PipelineRunStatusUpdateRequest {
  status: PipelineRunStatus
  imageTag?: string
  logText?: string
  errorMessage?: string
}

export type PipelineStepType =
  | 'SHELL'
  | 'GIT_CLONE'
  | 'MAVEN_BUILD'
  | 'DOCKER_BUILD'
  | 'DOCKER_PUSH'
  | 'DOCKER_DEPLOY'
  | 'HTTP_CHECK'

export interface PipelineDefinition {
  id: number
  projectId: number
  repositoryId: number
  name: string
  code: string
  description: string
  triggerType: PipelineTriggerType
  triggerTypeDescription: string
  enabled: boolean
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface PipelineDefinitionCreateRequest {
  projectId: number
  repositoryId: number
  name: string
  code: string
  description?: string
  triggerType: PipelineTriggerType
}

export interface PipelineDefinitionUpdateRequest {
  name: string
  description?: string
  triggerType: PipelineTriggerType
  repositoryId: number
  enabled?: boolean
}

export interface PipelineStepDefinition {
  id: number
  pipelineId: number
  name: string
  stepType: PipelineStepType
  stepTypeDescription: string
  sortOrder: number
  command: string
  configJson: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface PipelineStepDefinitionCreateRequest {
  name: string
  stepType: PipelineStepType
  sortOrder: number
  command?: string
  configJson?: string
  enabled?: boolean
}

export interface PipelineStepDefinitionUpdateRequest {
  name: string
  stepType: PipelineStepType
  sortOrder: number
  command?: string
  configJson?: string
  enabled?: boolean
}

export interface PipelineRunRecord {
  id: number
  pipelineId: number
  projectId: number
  repositoryId: number
  versionId?: number
  runNo: string
  branchName: string
  commitHash: string
  imageTag: string
  env: string
  status: PipelineRunStatus
  statusDescription: string
  triggerUserId?: number
  triggerType: PipelineTriggerType
  triggerTypeDescription: string
  startedAt: string
  finishedAt: string
  durationSeconds: number
  createdAt: string
  updatedAt: string
}

export interface PipelineRunRecordCreateRequest {
  versionId?: number
  branchName?: string
  commitHash?: string
  imageTag?: string
  env?: string
}

export interface PipelineRunRecordQueryRequest {
  pipelineId?: number
  projectId?: number
  repositoryId?: number
  versionId?: number
  status?: PipelineRunStatus
  triggerType?: PipelineTriggerType
}

export interface PipelineStepRunRecord {
  id: number
  pipelineRunId: number
  pipelineStepId?: number
  name: string
  stepType: PipelineStepType
  stepTypeDescription: string
  sortOrder: number
  command: string
  configJson: string
  status: PipelineRunStatus
  statusDescription: string
  startedAt: string
  finishedAt: string
  durationSeconds: number
  exitCode?: number
  errorMessage: string
  createdAt: string
  updatedAt: string
}

export interface PipelineLogRecord {
  id: number
  pipelineRunId: number
  stepRunId?: number
  logTime: string
  logLevel: string
  content: string
  createdAt: string
}

export type RunnerStatus = 'ONLINE' | 'OFFLINE' | 'BUSY' | 'DISABLED'

export interface RunnerRecord {
  id: number
  runnerName: string
  ip: string
  port: number
  status: RunnerStatus
  statusDescription: string
  maxConcurrency: number
  currentConcurrency: number
  lastHeartbeatAt: string
  registeredAt: string
  createdAt: string
  updatedAt: string
}

export interface RunnerStatusUpdatePayload {
  status: RunnerStatus
}
