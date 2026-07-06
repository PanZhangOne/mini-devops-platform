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
export type TaskType = 'REQUIREMENT' | 'TASK' | 'BUG' | 'STORY' | 'SUB_TASK'
export type TaskPropertyType =
  | 'TEXT'
  | 'NUMBER'
  | 'DATE'
  | 'SELECT'
  | 'MULTI_SELECT'
  | 'USER'
  | 'BOOLEAN'

export interface Task {
  id: number
  projectId: number
  moduleId: number | null
  parentTaskId: number | null
  taskNo: string
  title: string
  description: string
  taskType: TaskType
  taskTypeDescription: string
  assigneeId: number
  reporterId: number
  reporterName: string
  status: TaskStatus
  statusDescription: string
  priority: TaskPriority
  priorityDescription: string
  dueDate: string | null
  startedAt: string | null
  finishedAt: string | null
  estimatedHours: number | null
  actualHours: number | null
  sortOrder: number | null
  createdAt: string
  updatedAt: string
}

export interface TaskCreateRequest {
  projectId: number
  moduleId?: number
  parentId?: number
  title: string
  taskType: TaskType
  description?: string
  assigneeId?: number
  priority: TaskPriority
  reporterId?: number
  dueDate?: string
  estimatedHours?: number
  actualHours?: number
  sortOrder?: number
}

export interface TaskUpdateRequest {
  projectId: number
  moduleId?: number
  parentTaskId?: number
  title: string
  taskType: TaskType
  description?: string
  assigneeId?: number
  reporterId?: number
  status: TaskStatus
  priority: TaskPriority
  dueDate?: string
  estimatedHours?: number
  actualHours?: number
  sortOrder?: number
}

export interface TaskStatusChangeRequest {
  targetStatus: TaskStatus
  userId?: number
  remark?: string
}

export interface TaskProperty {
  id: number
  projectId: number
  name: string
  code: string
  propertyType: TaskPropertyType
  propertyTypeDescription: string
  required: boolean
  optionsJson: string | null
  sortOrder: number
  enabled: boolean
  createdBy: number | null
  createdAt: string
  updatedAt: string
}

export interface TaskPropertyCreateRequest {
  name: string
  code: string
  propertyType: TaskPropertyType
  required?: boolean
  optionsJson?: string
  sortOrder?: number
  enabled?: boolean
}

export interface TaskPropertyUpdateRequest extends TaskPropertyCreateRequest {}

export interface TaskPropertyValue {
  id: number | null
  taskId: number
  propertyId: number
  propertyCode: string
  propertyName: string
  propertyType: TaskPropertyType
  propertyTypeDescription: string
  required: boolean
  optionsJson: string | null
  valueText: string | null
  createdAt: string | null
  updatedAt: string | null
}

export interface TaskPropertyValueSaveItemRequest {
  propertyId: number
  valueText: string | null
}

export interface TaskPropertyValueSaveRequest {
  values: TaskPropertyValueSaveItemRequest[]
}

export interface TaskComment {
  id: number
  taskId: number
  parentId: number | null
  content: string
  createdBy: number | null
  createdAt: string
  updatedAt: string
}

export interface TaskCommentTree extends TaskComment {
  children: TaskCommentTree[]
}

export interface TaskCommentCreateRequest {
  parentId?: number
  content: string
}

export interface TaskActivity {
  id: number
  taskId: number
  actionType: string
  actionTypeDescription: string
  actionContent: string
  oldValue: string | null
  oldValueDescription: string | null
  newValue: string | null
  newValueDescription: string | null
  createdBy: number | null
  createdAt: string
}

export interface TaskDetail extends Task {
  children: Task[]
  propertyValues: TaskPropertyValue[]
  comments: TaskCommentTree[]
  activities: TaskActivity[]
}

export interface ProjectModule {
  id: number
  projectId: number
  parentId: number | null
  name: string
  code: string
  description: string | null
  sortOrder: number | null
  createdBy: number | null
  createdAt: string
  updatedAt: string
}

export interface ProjectModuleTree extends ProjectModule {
  children: ProjectModuleTree[]
}

export interface ProjectModuleCreateRequest {
  parentId: number
  name: string
  code: string
  description?: string
  sortOrder?: number
}

export interface ProjectModuleUpdateRequest {
  parentId?: number
  name: string
  code: string
  description?: string
  sortOrder?: number
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

// ─── Credential ───────────────────────────────────────────────────────────────

export type CredentialType = 'USERNAME_PASSWORD' | 'TOKEN'

export interface Credential {
  id: number
  projectId: number
  name: string
  credentialType: CredentialType
  credentialTypeDescription: string
  username: string
  description: string
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface CredentialCreateRequest {
  projectId: number
  name: string
  credentialType: CredentialType
  username?: string
  secretValue: string
  description?: string
}

export interface CredentialUpdateRequest {
  name: string
  credentialType: CredentialType
  username?: string
  secretValue?: string
  description?: string
}
