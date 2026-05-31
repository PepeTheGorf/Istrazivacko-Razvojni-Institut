export type WorkflowCondition = {
  id?: string
  field?: string
  operator?: string
  value?: string
}

export interface PhaseCreation {
  name: string
  description?: string
  order: number
  conditions?: WorkflowCondition[]
}

export interface WorkflowCreation {
  name: string
  description?: string
  phases: PhaseCreation[]
}

export interface Workflow {
  id?: string
  name: string
  description?: string
  phases?: PhaseCreation[]
}
