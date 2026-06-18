export interface WorkflowPhase {
  id?: number
  name: string
  description?: string
  order: number
  conditions?: WorkflowCondition[]
}

export type WorkflowCondition = {
  id?: number
  field?: string
  operator?: string
  value?: string
}

export interface PhaseCreation {
  id?: number
  name: string
  description?: string
  order: number
  conditions?: WorkflowCondition[]
}

export interface TransitionConditionType {
  id: number
  name: string
  description: string
}

export interface TransitionConditionCreation {
  from: string
  to: string
  transitionTypeId: number[]
}

export interface WorkflowCreation {
  name: string
  description?: string
  phases: PhaseCreation[]
  transitionConditions?: TransitionConditionCreation[]
}

export interface Workflow {
  id?: number
  name: string
  description?: string
  phases?: WorkflowPhase[]
  transitionConditions?: TransitionConditionCreation[]
}
