import { apiFetch } from './client'
import type {
  AcceptanceCriteriaPayload,
  AcceptanceCriterion,
  AssignedProjectSummary,
  AssignedTaskSummary,
  ProjectTask,
  TaskSummary,
  TaskAssignmentPayload,
  TaskCreationPayload,
  TaskTransitionsResponse,
} from '../types/task'

export function fetchMyAssignedProjects(): Promise<AssignedProjectSummary[]> {
  return apiFetch<AssignedProjectSummary[]>('/tasks/my-projects')
}

export function fetchMyTasksByProject(projectId: string): Promise<AssignedTaskSummary[]> {
  return apiFetch<AssignedTaskSummary[]>(`/tasks/my-tasks/${projectId}`)
}

export function fetchTasksByProject(projectId: string): Promise<TaskSummary[]> {
  return apiFetch<TaskSummary[]>(`/tasks/project/${projectId}`)
}

export function fetchTaskById(taskId: string): Promise<ProjectTask> {
  return apiFetch<ProjectTask>(`/tasks/${taskId}`)
}

export function createTask(payload: TaskCreationPayload): Promise<TaskSummary> {
  return apiFetch<TaskSummary>('/tasks', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function assignTaskToMember(payload: TaskAssignmentPayload): Promise<void> {
  return apiFetch<void>('/tasks/assign-member', {
    method: 'PUT',
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

export function fetchTaskTransitions(taskId: string): Promise<TaskTransitionsResponse> {
  return apiFetch<TaskTransitionsResponse>(`/tasks/transitions/${taskId}`)
}

export function moveTaskToPhase(taskId: string, phaseId: number): Promise<void> {
  return apiFetch<void>(`/tasks/${taskId}/move-to-phase/${phaseId}`, {
    method: 'PUT',
  })
}

export function toggleAcceptanceCriterion(id: string): Promise<void> {
  return apiFetch<void>(`/acceptance-criteria/${id}/solve`, {
    method: 'PUT',
  })
}
