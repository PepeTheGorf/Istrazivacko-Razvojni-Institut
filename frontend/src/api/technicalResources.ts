import { apiFetch } from './client'
import type { TaskResourceAssignment } from '../types/task'
import type { TechnicalResource, TechnicalResourcePayload } from '../types/technicalResource'

export function fetchTechnicalResources(): Promise<TechnicalResource[]> {
  return apiFetch<TechnicalResource[]>('/technical-resources/all')
}

export function createTechnicalResource(payload: TechnicalResourcePayload): Promise<void> {
  return apiFetch<void>('/technical-resources', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateTechnicalResource(
  resourceId: number,
  payload: TechnicalResourcePayload,
): Promise<void> {
  return apiFetch<void>(`/technical-resources/${resourceId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteTechnicalResource(resourceId: number): Promise<void> {
  return apiFetch<void>(`/technical-resources/${resourceId}`, {
    method: 'DELETE',
  })
}

export function assignTechnicalResourceToTask(
  resourceId: number,
  taskId: number,
  quantity: number,
): Promise<void> {
  return apiFetch<void>(
    `/technical-resources/assign/${resourceId}/to-task/${taskId}/quantity/${quantity}`,
    { method: 'POST' },
  )
}

export function fetchTechnicalResourcesByTask(taskId: number): Promise<TaskResourceAssignment[]> {
  return apiFetch<TaskResourceAssignment[]>(`/technical-resources/task/${taskId}`)
}
