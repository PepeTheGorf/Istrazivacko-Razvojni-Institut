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

export function getTasksForPhaseName(
  projectTasks: TaskSummary[],
  phaseName: string,
  filters?: Partial<AnalyticsFilters>,
): TaskSummary[] {
  return flattenTasks(projectTasks).filter(
    (task) => task.phaseName === phaseName && matchesTaskFilters(task, filters),
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

export function phaseSectionKey(phaseOrder: number, phaseName: string) {
  return `${phaseOrder}:${phaseName}`
}
