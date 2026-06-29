import { apiFetch } from './client'
import type {
  AnalyticsFilters,
  ProjectWorkflowAnalysis,
  TaskTeamMemberStats,
} from '../types/analytics'

function buildAnalyticsQuery(filters?: Partial<AnalyticsFilters>) {
  const params = new URLSearchParams()
  if (filters?.from) params.set('from', filters.from)
  if (filters?.to) params.set('to', filters.to)
  if (filters?.memberId) params.set('memberId', filters.memberId)
  if (filters?.taskId) params.set('taskId', filters.taskId)
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function fetchProjectWorkflowAnalytics(
  projectId: string | number,
  filters?: Partial<AnalyticsFilters>,
) {
  return apiFetch<ProjectWorkflowAnalysis>(
    `/analytics/projects/${projectId}/workflow${buildAnalyticsQuery(filters)}`,
  )
}

export function fetchProjectTeamMemberStats(
  projectId: string | number,
  filters?: Partial<AnalyticsFilters>,
) {
  return apiFetch<TaskTeamMemberStats[]>(
    `/analytics/projects/${projectId}/team-members${buildAnalyticsQuery(filters)}`,
  )
}
