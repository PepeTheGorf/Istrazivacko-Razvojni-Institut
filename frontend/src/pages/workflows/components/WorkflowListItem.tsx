import { Link } from 'react-router-dom'
import { PhasePipelinePreview } from '../../../components/workflow/PhasePipelinePreview'
import { Button } from '../../../components/ui/Button'
import { resolveWorkflowPhases } from '../../../lib/workflowPhases'
import type { Workflow } from '../../../types/workflow'
interface WorkflowListItemProps {
  workflow: Workflow
  onDelete: (workflow: Workflow) => void
}

export function WorkflowListItem({ workflow, onDelete }: WorkflowListItemProps) {
  const phases = resolveWorkflowPhases(workflow)
  const editPath = workflow.id ? `/workflows/${workflow.id}/edit` : '/workflows'

  return (
    <article className="grid grid-cols-1 items-start gap-4 border-b border-hairline px-5 py-4 last:border-b-0 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1.4fr)_auto] lg:gap-5">
      <div className="min-w-0">
        <h2 className="m-0 text-xl leading-snug font-semibold text-ink md:text-2xl">
          {workflow.name}
        </h2>
        <p className="m-0 mt-1.5 text-sm leading-relaxed break-words text-ink-subtle">
          {workflow.description?.trim() || 'Standardni tok za razvoj softvera'}
        </p>
      </div>

      <PhasePipelinePreview phases={phases} className="self-center lg:justify-center" />

      <div className="flex shrink-0 gap-2 self-center lg:justify-end">
        <Link to={editPath} className="inline-flex">
          <Button variant="secondary" type="button" className="min-w-24" icon="edit">
            Izmeni
          </Button>
        </Link>
        <Button
          variant="delete"
          type="button"
          icon="delete"
          className="min-w-24 border border-error/45"
          onClick={() => onDelete(workflow)}
        >
          Obriši
        </Button>
      </div>
    </article>
  )
}
