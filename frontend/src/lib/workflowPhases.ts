import { defaultPhases } from '../components/workflow/workflowWizardConfig'
import type { Workflow } from '../types/workflow'

export function resolveWorkflowPhases(workflow: Workflow): Array<{ name: string; order: number }> {
  if (workflow.phases && workflow.phases.length > 0) {
    return [...workflow.phases]
      .map((p, index) => ({ name: p.name, order: p.order ?? index + 1 }))
      .sort((a, b) => a.order - b.order)
  }
  return defaultPhases()
}
