import { defaultPhases } from '../components/workflow/workflowWizardConfig'
import type { PhaseCreation, Workflow } from '../types/workflow'

export function resolveWorkflowPhases(workflow: Workflow): PhaseCreation[] {
  if (workflow.phases && workflow.phases.length > 0) {
    return [...workflow.phases]
      .map((phase, index) => ({
        id: phase.id,
        name: phase.name,
        order: phase.order ?? index + 1,
      }))
      .sort((a, b) => a.order - b.order)
  }
  return defaultPhases()
}
