import { apiFetch } from './client'
import type {
  AcceptanceCriteriaPayload,
  AcceptanceCriterion,
  ProjectTask,
  TaskCreationPayload,
} from '../types/task'

export function fetchTasksByProject(projectId: string): Promise<ProjectTask[]> {
  return apiFetch<ProjectTask[]>(`/tasks/project/${projectId}`)
}

export function fetchTaskById(taskId: string): Promise<ProjectTask> {
  return apiFetch<ProjectTask>(`/tasks/${taskId}`)
}

export function createTask(payload: TaskCreationPayload): Promise<void> {
  return apiFetch<void>('/tasks', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateTask(taskId: string, payload: TaskCreationPayload): Promise<void> {
  return apiFetch<void>(`/tasks/${taskId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteTask(taskId: string): Promise<void> {
  return apiFetch<void>(`/tasks/${taskId}`, {
    method: 'DELETE',
  })
}

export function createAcceptanceCriterion(payload: AcceptanceCriteriaPayload): Promise<void> {
  return apiFetch<void>('/acceptance-criteria', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateAcceptanceCriterion(id: string, payload: AcceptanceCriteriaPayload): Promise<void> {
  return apiFetch<void>(`/acceptance-criteria/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteAcceptanceCriterion(id: string): Promise<void> {
  return apiFetch<void>(`/acceptance-criteria/${id}`, {
    method: 'DELETE',
  })
}

export function fetchAcceptanceCriteria(taskId: string): Promise<AcceptanceCriterion[]> {
  return apiFetch<AcceptanceCriterion[]>(`/acceptance-criteria?taskId=${encodeURIComponent(taskId)}`)
}
