import type { TaskSummary } from '../../../types/task'
import type { AnalyticsFilters } from '../../../types/analytics'

export function flattenTasks(tasks: TaskSummary[]): TaskSummary[] {
  return tasks.flatMap((task) => [task, ...flattenTasks(task.subTasks ?? [])])
}

function matchesTaskFilters(task: TaskSummary, filters?: Partial<AnalyticsFilters>) {
  if (!filters) return true

  if (filters.memberId) {
    const memberId = Number(filters.memberId)
    if (!task.assigneeIds?.includes(memberId)) return false
  }

  if (filters.taskId && String(task.id) !== filters.taskId) {
    return false
  }

  return true
}

function matchesPhase(
  task: TaskSummary,
  phaseId: number,
  phaseName: string,
  phaseOrder: number,
) {
  if (task.phaseId != null) {
    return task.phaseId === phaseId
  }

  return task.phaseName === phaseName && task.phaseOrder === phaseOrder
}

export function getTasksForPhase(
  projectTasks: TaskSummary[],
  phaseId: number,
  phaseName: string,
  phaseOrder: number,
  filters?: Partial<AnalyticsFilters>,
): TaskSummary[] {
  return flattenTasks(projectTasks).filter(
    (task) =>
      matchesPhase(task, phaseId, phaseName, phaseOrder) &&
      matchesTaskFilters(task, filters),
  )
}

export function getTasksForMemberId(
  projectTasks: TaskSummary[],
  memberId: number,
  filters?: Partial<AnalyticsFilters>,
): TaskSummary[] {
  return flattenTasks(projectTasks).filter(
    (task) => task.assigneeIds?.includes(memberId) && matchesTaskFilters(task, filters),
  )
}

export function phaseSectionKey(phaseId: number) {
  return String(phaseId)
}

export type MemberTaskStatusFilter = 'completed' | 'active' | 'overdue'

export function matchesMemberTaskStatus(
  task: TaskSummary,
  statusFilter?: MemberTaskStatusFilter | null,
) {
  if (!statusFilter) return true
  if (statusFilter === 'completed') return Boolean(task.completed)
  if (statusFilter === 'active') return !task.completed
  if (statusFilter === 'overdue') return Boolean(task.overdue)
  return true
}

export function filterMemberTasksByStatus(
  tasks: TaskSummary[],
  statusFilter?: MemberTaskStatusFilter | null,
) {
  if (!statusFilter) return tasks
  return tasks.filter((task) => matchesMemberTaskStatus(task, statusFilter))
}
