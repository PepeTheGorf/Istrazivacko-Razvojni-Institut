import { apiFetch } from './client'
import type { Workflow, WorkflowCreation } from '../types/workflow'

export function fetchWorkflows(): Promise<Workflow[]> {
  return apiFetch<Workflow[]>('/workflows')
}

export function createWorkflow(workflow: WorkflowCreation): Promise<void> {
  return apiFetch<void>('/workflows', {
    method: 'POST',
    body: JSON.stringify(workflow),
  })
}

export function updateWorkflow(workflowId: string, workflow: WorkflowCreation): Promise<void> {
  return apiFetch<void>(`/workflows/${workflowId}`, {
    method: 'PUT',
    body: JSON.stringify(workflow),
  })
}

export function deleteWorkflow(workflowId: string): Promise<void> {
  return apiFetch<void>(`/workflows/${workflowId}`, {
    method: 'DELETE',
  })
}

