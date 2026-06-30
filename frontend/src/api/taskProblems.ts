import { apiFetch } from './client'

export type ProblemStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'

export type ProblemType = 'TECHNICAL' | 'TEAM' | 'OTHER'

export interface ProblemReport {
  id?: number
  taskId?: number
  taskName?: string
  creatorId?: number
  reviewedById?: number
  description?: string
  problemType?: ProblemType
  status?: ProblemStatus
  reportedAt?: string
}

export interface ProblemReportPayload {
  taskId: number
  description: string
  problemType: ProblemType
}

export interface ProblemReportUpdatePayload {
  status?: ProblemStatus
  description?: string
  problemType?: ProblemType
  reviewedById?: number
}

export function fetchProblemsByTask(taskId: number): Promise<ProblemReport[]> {
  return apiFetch<ProblemReport[]>(`/task-problems?taskId=${taskId}`)
}

export function fetchMyProblemReports(): Promise<ProblemReport[]> {
  return apiFetch<ProblemReport[]>('/task-problems/my')
}

export function reportProblem(payload: ProblemReportPayload): Promise<void> {
  return apiFetch<void>('/task-problems', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProblemReport(
  problemId: number,
  payload: ProblemReportUpdatePayload,
): Promise<void> {
  return apiFetch<void>(`/task-problems/${problemId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
