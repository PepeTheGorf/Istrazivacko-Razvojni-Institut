import type { AssignedTaskSummary } from '../../../types/task'

export interface PhaseColumn {
  id: string
  title: string
  defaultExpanded: boolean
  tasks: AssignedTaskSummary[]
}

export function groupTasksByPhase(tasks: AssignedTaskSummary[]): PhaseColumn[] {
  const byPhase = new Map<string, AssignedTaskSummary[]>()

  for (const task of tasks) {
    const phase = task.phaseName?.trim() || 'Bez faze'
    const existing = byPhase.get(phase) ?? []
    existing.push(task)
    byPhase.set(phase, existing)
  }

  return Array.from(byPhase.entries()).map(([title, phaseTasks], index) => ({
    id: title,
    title,
    defaultExpanded: index < 2,
    tasks: phaseTasks,
  }))
}
