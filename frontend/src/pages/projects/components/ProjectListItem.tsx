import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import type { Project } from '../../../types/project'

interface ProjectListItemProps {
  project: Project
  canManage: boolean
  onDelete: (project: Project) => void
}

export function ProjectListItem({ project, canManage, onDelete }: ProjectListItemProps) {
  const editPath = project.id ? `/projects/${project.id}/edit` : '/projects'
  const detailsPath = project.id ? `/projects/${project.id}` : '/projects'

  return (
    <article className="grid grid-cols-1 items-start gap-4 px-5 py-5 transition-colors group-hover:bg-surface-2 lg:grid-cols-[minmax(0,1.1fr)_auto] lg:gap-5">
      <Link
        to={detailsPath}
        className="min-w-0 rounded-md border border-transparent p-1"
      >
        <h2 className="m-0 text-xl leading-snug font-semibold text-ink transition-colors group-hover:text-primary-hover md:text-2xl">
          {project.name}
        </h2>
        <p className="m-0 mt-1.5 text-sm leading-relaxed break-words text-ink-subtle">
          {project.description?.trim() || 'Opis nije unet.'}
        </p>
      </Link>

      {canManage && (
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
            onClick={() => onDelete(project)}
          >
            Obriši
          </Button>
        </div>
      )}
    </article>
  )
}

