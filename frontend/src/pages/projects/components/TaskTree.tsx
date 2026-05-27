import { Link } from 'react-router-dom'
import { formatDate } from '../../../lib/formatDate'
import type { ProjectTask } from '../../../types/task'

interface TaskTreeProps {
  tasks: ProjectTask[]
  projectId: string
}

interface TaskNodeProps {
  task: ProjectTask
  projectId: string
  depth: number
}

function TaskNode({ task, projectId, depth }: TaskNodeProps) {
  const taskHref = task.id ? `/projects/${projectId}/tasks/${task.id}` : undefined
  const hasChildren = (task.subTasks?.length ?? 0) > 0

  const cardContent = (
    <div className="flex flex-wrap items-start justify-between gap-3">
      <div className="min-w-0">
        <h3 className="m-0 text-base font-semibold text-ink transition-colors group-hover:text-primary-hover">
          {task.name}
        </h3>
        <p className="m-0 mt-1 text-sm text-ink-subtle">
          {task.description?.trim() || 'Opis zadatka nije unet.'}
        </p>
      </div>
      <div className="flex shrink-0 flex-wrap items-center gap-2">
        <span className="rounded-full border border-hairline px-2 py-1 text-xs text-ink-muted">
          {task.phaseName || 'Bez faze'}
        </span>
        {task.endDate ? (
          <span className="text-xs text-ink-subtle">Rok: {formatDate(task.endDate)}</span>
        ) : null}
      </div>
    </div>
  )

  return (
    <div className={depth > 0 ? 'mt-3 border-l border-hairline pl-4' : ''}>
      {taskHref ? (
        <Link
          to={taskHref}
          className="group block rounded-lg border border-hairline bg-surface-2 px-4 py-3 transition-colors hover:border-hairline-strong hover:bg-surface-3"
        >
          {cardContent}
        </Link>
      ) : (
        <article className="rounded-lg border border-hairline bg-surface-2 px-4 py-3">
          {cardContent}
        </article>
      )}

      {hasChildren ? (
        <div className="mt-2 space-y-2">
          {task.subTasks?.map((subTask) => (
            <TaskNode
              key={subTask.id ?? `${task.id ?? task.name}-${subTask.name}`}
              task={subTask}
              projectId={projectId}
              depth={depth + 1}
            />
          ))}
        </div>
      ) : null}
    </div>
  )
}

export function TaskTree({ tasks, projectId }: TaskTreeProps) {
  if (tasks.length === 0) {
    return <p className="m-0 text-sm text-ink-subtle">Još nema zadataka na projektu.</p>
  }

  return (
    <div className="space-y-4">
      {tasks.map((task) => (
        <TaskNode key={task.id ?? task.name} task={task} projectId={projectId} depth={0} />
      ))}
    </div>
  )
}
