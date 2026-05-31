import { apiFetch } from './client'

export type ProblemType = 'TECHNICAL' | 'TEAM' | 'OTHER'

export interface ProblemReport {
  id?: string
  taskId?: string
  creatorId?: number
  reviewedById?: number
  description?: string
  problemType?: ProblemType
  status?: string
  reportedAt?: string
}

export interface ProblemReportPayload {
  taskId: string
  description: string
  problemType: ProblemType
}

export function fetchProblemsByTask(taskId: string): Promise<ProblemReport[]> {
  return apiFetch<ProblemReport[]>(`/task-problems?taskId=${encodeURIComponent(taskId)}`)
}

export function reportProblem(payload: ProblemReportPayload): Promise<void> {
  return apiFetch<void>('/task-problems', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
