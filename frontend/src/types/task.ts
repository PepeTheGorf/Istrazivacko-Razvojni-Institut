import type { Workflow, WorkflowPhase } from './workflow'

export interface TaskResourceAssignment {
  resourceId: number
  name: string
  description?: string
  assignedQuantity: number
  availableQuantity: number
}

export interface TaskPhaseTransition {
  toPhaseId: number
  toPhaseName: string
  routeExists: boolean
  conditionsMet: boolean
  requirements: Array<{ id: number; name: string; description?: string; met: boolean }>
}

export interface TaskTransitionsResponse {
  currentPhaseId?: number
  currentPhaseName?: string
  workflowPhases: WorkflowPhase[]
  transitions: TaskPhaseTransition[]
}

export interface AcceptanceCriterion {
  id?: number
  taskId?: number
  name: string
  description?: string
  completed?: boolean
  creatorId?: number
}

export interface ProjectTask {
  id?: number
  name: string
  description?: string
  phaseName?: string
  startDate?: string
  endDate?: string
  creatorId?: number
  assigneeId?: number
  assigneeIds?: number[]
  workflow?: Workflow
  technicalResources?: TaskResourceAssignment[]
  acceptanceCriteria?: AcceptanceCriterion[]
  subTasks?: ProjectTask[]
}

export interface TaskSummary {
  id?: number
  name: string
  description?: string
  phaseName?: string
  endDate?: string
  assigneeNames?: string[]
  subTasks?: TaskSummary[]
}

export interface AssignedProjectSummary {
  id: number
  name: string
}

export interface AssignedTaskSummary {
  id?: number
  name: string
  description?: string
  phaseName?: string
  endDate?: string
  projectId?: number
  projectName?: string
}

export interface TaskCreationPayload {
  name: string
  description?: string
  endDate?: string
  projectId: number
  assigneeId?: number
  creatorId?: number
  parentTaskId?: number
  workflowId?: number
}

export interface TaskAssignmentPayload {
  taskId: number
  userId: number
}

export interface AcceptanceCriteriaPayload {
  taskId: number
  name: string
  description?: string
  creatorId?: number
}
