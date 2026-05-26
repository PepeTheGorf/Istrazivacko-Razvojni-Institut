import type { PhaseCreation } from '../../types/workflow'

export type WizardTabKey = 'basic' | 'phases' | 'conditions' | 'review'

export const WIZARD_TABS: WizardTabKey[] = ['basic', 'phases', 'conditions', 'review']

export const WIZARD_TAB_LABELS: Record<WizardTabKey, string> = {
  basic: 'Osnovno',
  phases: 'Faze',
  conditions: 'Uslovi',
  review: 'Pregled',
}

export function phaseRoleLabel(index: number, total: number): string {
  if (index === 0) return 'Početna'
  if (index === total - 1) return 'Krajnja'
  return 'Međufaza'
}

export function defaultPhases(): PhaseCreation[] {
  return [
    { name: 'TODO', order: 1 },
    { name: 'In Progress', order: 2 },
    { name: 'In Review', order: 3 },
    { name: 'Done', order: 4 },
  ]
}
