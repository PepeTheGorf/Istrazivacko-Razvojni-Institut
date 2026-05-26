import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import type { Workflow } from '../../../types/workflow'
import { WorkflowListItem } from './WorkflowListItem'

interface WorkflowListProps {
  workflows: Workflow[]
  loading: boolean
  onDelete: (workflow: Workflow) => void
}

export function WorkflowList({ workflows, loading, onDelete }: WorkflowListProps) {
  if (loading) {
    return <p className="m-0 text-ink-subtle">Učitavanje…</p>
  }

  if (workflows.length === 0) {
    return (
      <section className="rounded-lg border border-hairline bg-surface-1 p-8 text-center">
        <p className="m-0 text-ink-subtle">Nema tokova rada za prikaz.</p>
        <Link to="/workflows/new" className="mt-4 inline-flex">
          <Button type="button" variant="secondary" icon="add">
            Kreiraj prvi tok
          </Button>
        </Link>
      </section>
    )
  }

  return (
    <div className="overflow-hidden rounded-xl border border-hairline bg-surface-1">
      {workflows.map((workflow) => (
        <WorkflowListItem
          key={workflow.id ?? workflow.name}
          workflow={workflow}
          onDelete={onDelete}
        />
      ))}
    </div>
  )
}
