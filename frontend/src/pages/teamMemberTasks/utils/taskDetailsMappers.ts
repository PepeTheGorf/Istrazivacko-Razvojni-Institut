import type { AcceptanceCriterion, ProjectTask } from '../../../types/task'

export interface DisplaySubtask {
  id: string
  title: string
  status: string
  children?: DisplaySubtask[]
}

export interface DisplayCriterion {
  id: string
  label: string
  checked: boolean
}

export function mapSubtask(task: ProjectTask): DisplaySubtask {
  return {
    id: String(task.id),
    title: task.name,
    status: task.phaseName ?? 'Bez faze',
    children: task.subTasks?.map(mapSubtask),
  }
}

export function mapCriteria(criteria: AcceptanceCriterion[] | undefined): DisplayCriterion[] {
  return (criteria ?? []).map((item) => ({
    id: String(item.id ?? item.name),
    label: item.name,
    checked: Boolean(item.completed),
  }))
}
