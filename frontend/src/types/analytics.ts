export interface PhaseAnalytics {
  phaseId: number
  phaseName: string
  phaseOrder: number
  currentTaskCount: number
  averageSecondsInPhase: number
}

export interface TaskPhaseHistoryEntry {
  fromPhaseName: string
  toPhaseName: string
  durationSeconds: number
  transitionedAt: string
}

export interface ProjectWorkflowAnalysis {
  projectId: number
  projectName: string
  phaseAnalytics: PhaseAnalytics[]
  totalTasks: number
  completedTasks: number
  activeTasks: number
  overdueTasks: number
  totalTaskDurationSeconds: number
  averageTaskDurationSeconds: number
  taskPhaseHistory: TaskPhaseHistoryEntry[]
}

export interface TaskTeamMemberStats {
  memberId: number
  memberName: string
  totalAssignedTasks: number
  completedTasks: number
  activeTasks: number
  overdueTasks: number
  averageTaskDurationSeconds: number
}

export type AnalyticsStatisticType = 'workflow' | 'team'

export interface AnalyticsFilters {
  from: string
  to: string
  memberId: string
  taskId: string
}

export const emptyAnalyticsFilters: AnalyticsFilters = {
  from: '',
  to: '',
  memberId: '',
  taskId: '',
}
