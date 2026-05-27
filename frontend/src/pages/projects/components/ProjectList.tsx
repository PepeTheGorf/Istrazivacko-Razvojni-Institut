import type { Project } from '../../../types/project'
import { ProjectListItem } from './ProjectListItem'

interface ProjectListProps {
  projects: Project[]
  loading: boolean
  canManage: boolean
  onDelete: (project: Project) => void
}

export function ProjectList({
  projects,
  loading,
  canManage,
  onDelete,
}: ProjectListProps) {
  if (loading) {
    return (
      <section className="rounded-xl border border-hairline bg-surface-1">
        <p className="m-0 px-5 py-6 text-sm text-ink-subtle">Učitavanje…</p>
      </section>
    )
  }

  if (projects.length === 0) {
    return (
      <section className="rounded-xl border border-hairline bg-surface-1">
        <p className="m-0 px-5 py-6 text-sm text-ink-subtle">Još nema projekata.</p>
      </section>
    )
  }

  return (
    <section className="rounded-xl border border-hairline bg-surface-1">
      <ul className="m-0 list-none divide-y divide-hairline-strong p-0">
        {projects.map((project) => (
          <li key={project.id ?? project.name} className="group">
            <ProjectListItem
              project={project}
              canManage={canManage}
              onDelete={onDelete}
            />
          </li>
        ))}
      </ul>
    </section>
  )
}

