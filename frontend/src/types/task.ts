import type { Workflow } from './workflow'

export interface AcceptanceCriterion {
  id?: string
  taskId?: string
  name: string
  description?: string
  completed?: boolean
  creatorId?: number
}

export interface ProjectTask {
  id?: string
  name: string
  description?: string
  phaseName?: string
  startDate?: string
  endDate?: string
  creatorId?: number
  assigneeId?: number
  workflow?: Workflow
  technicalResources?: Array<{ id?: string; name?: string; description?: string }>
  acceptanceCriteria?: AcceptanceCriterion[]
  subTasks?: ProjectTask[]
}

export interface TaskCreationPayload {
  name: string
  description?: string
  endDate?: string
  projectId: string
  assigneeId?: number
  creatorId?: number
  parentTaskId?: string
  workflowId?: string
}

export interface AcceptanceCriteriaPayload {
  taskId: string
  name: string
  description?: string
  creatorId?: number
}
