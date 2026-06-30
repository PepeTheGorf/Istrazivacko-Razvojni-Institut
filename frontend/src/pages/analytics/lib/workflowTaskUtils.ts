import type { TaskSummary } from '../../../types/task'
import type { AnalyticsFilters } from '../../../types/analytics'
import { toIsoDateTimeOrUndefined } from '../../../lib/datetimeInput'

export function flattenTasks(tasks: TaskSummary[]): TaskSummary[] {
  return tasks.flatMap((task) => [task, ...flattenTasks(task.subTasks ?? [])])
}

function parseTime(value?: string) {
  if (!value) return null
  const time = new Date(value).getTime()
  return Number.isNaN(time) ? null : time
}

function matchesDateFilters(task: TaskSummary, filters?: Partial<AnalyticsFilters>) {
  const from = parseTime(toIsoDateTimeOrUndefined(filters?.from ?? ''))
  const to = parseTime(toIsoDateTimeOrUndefined(filters?.to ?? ''))
  if (from == null && to == null) return true

  const end = parseTime(task.endDate)
  const start = parseTime(task.startDate)

  if (from != null && end != null && end < from) return false
  if (to != null && start != null && start > to) return false

  return true
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

  if (!matchesDateFilters(task, filters)) {
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
  const normalizedTaskPhaseId =
    task.phaseId != null && task.phaseId !== undefined ? Number(task.phaseId) : null
  const normalizedPhaseId = Number(phaseId)

  if (normalizedTaskPhaseId != null && !Number.isNaN(normalizedTaskPhaseId)) {
    return normalizedTaskPhaseId === normalizedPhaseId
  }

  return (
    task.phaseName === phaseName &&
    (task.phaseOrder == null || task.phaseOrder === phaseOrder)
  )
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

export function getFilteredProjectTasks(
  projectTasks: TaskSummary[],
  filters?: Partial<AnalyticsFilters>,
): TaskSummary[] {
  return flattenTasks(projectTasks).filter((task) => matchesTaskFilters(task, filters))
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
