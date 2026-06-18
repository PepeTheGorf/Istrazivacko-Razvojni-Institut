import { Button } from '../../../components/ui/Button'
import type { TechnicalResource } from '../../../types/technicalResource'

interface TechnicalResourceListProps {
  resources: TechnicalResource[]
  loading: boolean
  onEdit: (resource: TechnicalResource) => void
  onDelete: (resource: TechnicalResource) => void
}

export function TechnicalResourceList({
  resources,
  loading,
  onEdit,
  onDelete,
}: TechnicalResourceListProps) {
  if (loading) {
    return <p className="m-0 text-ink-subtle">Učitavanje…</p>
  }

  if (resources.length === 0) {
    return (
      <section className="rounded-lg border border-hairline bg-surface-1 p-8 text-center">
        <p className="m-0 text-ink-subtle">Nema tehničkih resursa za prikaz.</p>
      </section>
    )
  }

  return (
    <div className="overflow-hidden rounded-xl border border-hairline bg-surface-1">
      {resources.map((resource) => (
        <article
          key={resource.id ?? resource.name}
          className="flex flex-wrap items-center justify-between gap-3 border-b border-hairline px-4 py-3 last:border-b-0"
        >
          <div className="min-w-0">
            <p className="m-0 text-sm font-medium text-ink">{resource.name}</p>
            {resource.description?.trim() ? (
              <p className="m-0 mt-1 text-sm text-ink-subtle">{resource.description}</p>
            ) : null}
            <p className="m-0 mt-1 text-xs text-ink-tertiary">
              Dostupna količina: {resource.quantity ?? 0}
            </p>
          </div>
          <div className="flex gap-2">
            <Button type="button" variant="secondary" icon="edit" onClick={() => onEdit(resource)}>
              Izmeni
            </Button>
            <Button
              type="button"
              variant="delete"
              icon="delete"
              className="border border-error/45"
              onClick={() => onDelete(resource)}
            >
              Obriši
            </Button>
          </div>
        </article>
      ))}
    </div>
  )
}
