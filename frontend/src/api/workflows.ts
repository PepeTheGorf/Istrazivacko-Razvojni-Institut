import { apiFetch } from './client'
import type { TransitionConditionType, Workflow, WorkflowCreation } from '../types/workflow'

export function fetchTransitionConditionTypes(): Promise<TransitionConditionType[]> {
  return apiFetch<TransitionConditionType[]>('/workflows/transition-condition-types')
}

export function fetchWorkflows(): Promise<Workflow[]> {
  return apiFetch<Workflow[]>('/workflows')
}

export function fetchWorkflowById(workflowId: number): Promise<Workflow> {
  return apiFetch<Workflow>(`/workflows/${workflowId}`)
}

export function createWorkflow(workflow: WorkflowCreation): Promise<void> {
  return apiFetch<void>('/workflows', {
    method: 'POST',
    body: JSON.stringify(workflow),
  })
}

export function updateWorkflow(workflowId: number, workflow: WorkflowCreation): Promise<void> {
  return apiFetch<void>(`/workflows/${workflowId}`, {
    method: 'PUT',
    body: JSON.stringify(workflow),
  })
}

export function deleteWorkflow(workflowId: number): Promise<void> {
  return apiFetch<void>(`/workflows/${workflowId}`, {
    method: 'DELETE',
  })
}

