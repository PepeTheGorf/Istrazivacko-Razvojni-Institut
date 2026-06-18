import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { formatDate } from '../../../lib/formatDate'
import type { TaskSummary } from '../../../types/task'

interface TaskTreeProps {
  tasks: TaskSummary[]
  projectId: string
}

interface TaskNodeProps {
  task: TaskSummary
  projectId: string
  depth: number
  nodeKey: string
  collapsedTaskKeys: Set<string>
  onToggleTask: (nodeKey: string) => void
}

interface FlatTaskItem {
  task: TaskSummary
  depth: number
}

function normalize(value?: string): string {
  return value?.trim().toLowerCase() ?? ''
}

function parseDateOnly(value?: string): number | null {
  if (!value) return null
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return null
  return Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate())
}

function parseLocalDateInput(value: string): number | null {
  if (!value) return null
  const date = new Date(`${value}T00:00:00`)
  if (Number.isNaN(date.getTime())) return null
  return Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())
}

function dateMatches(task: TaskSummary, dateFrom: string, dateTo: string): boolean {
  const from = parseLocalDateInput(dateFrom)
  const to = parseLocalDateInput(dateTo)
  if (from === null && to === null) return true

  const taskDate = parseDateOnly(task.endDate)
  if (taskDate === null) return false
  if (from !== null && taskDate < from) return false
  if (to !== null && taskDate > to) return false
  return true
}

function taskMatches(
  task: TaskSummary,
  query: string,
  phaseFilter: string,
  dateFrom: string,
  dateTo: string,
): boolean {
  const phase = task.phaseName || 'Bez faze'
  const phaseMatches = phaseFilter === 'ALL' || phase === phaseFilter
  if (!phaseMatches) return false
  if (!dateMatches(task, dateFrom, dateTo)) return false
  if (!query) return true

  return [task.name, task.description, task.phaseName].some((part) => normalize(part).includes(query))
}

function filterTaskTree(
  tasks: TaskSummary[],
  query: string,
  phaseFilter: string,
  dateFrom: string,
  dateTo: string,
): TaskSummary[] {
  return tasks.reduce<TaskSummary[]>((acc, task) => {
    const filteredChildren = filterTaskTree(task.subTasks ?? [], query, phaseFilter, dateFrom, dateTo)
    const currentMatches = taskMatches(task, query, phaseFilter, dateFrom, dateTo)

    if (currentMatches || filteredChildren.length > 0) {
      acc.push({ ...task, subTasks: filteredChildren })
    }
    return acc
  }, [])
}

function flattenTasks(tasks: TaskSummary[], depth = 0): FlatTaskItem[] {
  return tasks.flatMap((task) => [{ task, depth }, ...flattenTasks(task.subTasks ?? [], depth + 1)])
}

function countAllTasks(tasks: TaskSummary[]): number {
  return tasks.reduce((total, task) => total + 1 + countAllTasks(task.subTasks ?? []), 0)
}

function ChevronIcon({ expanded }: { expanded: boolean }) {
  return (
    <svg
      viewBox="0 0 20 20"
      aria-hidden
      className={`h-4 w-4 text-ink-subtle transition-transform ${expanded ? 'rotate-90' : 'rotate-0'}`}
    >
      <path
        d="M7 5l6 5-6 5"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function TaskNode({
  task,
  projectId,
  depth,
  nodeKey,
  collapsedTaskKeys,
  onToggleTask,
}: TaskNodeProps) {
  const taskHref = task.id ? `/projects/${projectId}/tasks/${task.id}` : undefined
  const hasChildren = (task.subTasks?.length ?? 0) > 0
  const isRoot = depth === 0
  const isExpanded = !collapsedTaskKeys.has(nodeKey)

  const rowContent = (
    <div className="flex min-w-0 flex-1 flex-wrap items-start justify-between gap-2">
      <div className="min-w-0 flex-1">
        <h3
          className={
            isRoot
              ? 'm-0 text-base font-semibold leading-snug text-ink transition-colors group-hover:text-primary-hover'
              : 'm-0 text-sm font-medium leading-snug text-ink transition-colors group-hover:text-primary-hover'
          }
        >
          {task.name}
        </h3>
        {task.description?.trim() ? (
          <p
            className={
              isRoot
                ? 'm-0 mt-0.5 text-sm leading-snug text-ink-subtle'
                : 'm-0 mt-0.5 text-xs leading-snug text-ink-subtle'
            }
          >
            {task.description}
          </p>
        ) : null}
      </div>
      <div className="flex shrink-0 flex-wrap items-center gap-1.5">
        <span className="rounded-full border border-hairline px-1.5 py-0.5 text-[10px] text-ink-muted">
          {task.phaseName || 'Bez faze'}
        </span>
        {task.endDate ? <span className="text-[10px] text-ink-subtle">{formatDate(task.endDate)}</span> : null}
      </div>
    </div>
  )

  const rowClassName = isRoot
    ? 'group block rounded-md border border-hairline bg-surface-2 px-3 py-2.5 transition-colors hover:border-hairline-strong hover:bg-surface-3'
    : 'group block rounded-sm px-2 py-1.5 transition-colors hover:bg-surface-2'

  const rowBody = taskHref ? (
    <Link to={taskHref} className={rowClassName}>
      {rowContent}
    </Link>
  ) : (
    <article className={rowClassName}>{rowContent}</article>
  )

  return (
    <div className={isRoot ? 'task-thread__root' : 'task-thread__node'}>
      <div className="flex items-center gap-2">
        {hasChildren ? (
          <button
            type="button"
            onClick={(event) => {
              event.preventDefault()
              event.stopPropagation()
              onToggleTask(nodeKey)
            }}
            aria-label={isExpanded ? 'Sakrij podzadatke' : 'Prikaži podzadatke'}
            className="inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-sm transition-colors hover:bg-surface-3"
          >
            <ChevronIcon expanded={isExpanded} />
          </button>
        ) : null}
        <div className="min-w-0 flex-1">{rowBody}</div>
      </div>

      {hasChildren ? (
        <div
          className={`task-thread__collapse overflow-hidden transition-all duration-250 ease-out ${isExpanded ? 'max-h-[4000px] opacity-100' : 'max-h-0 opacity-0'}`}
        >
          <div className="task-thread__replies">
            {task.subTasks?.map((subTask, index) => (
              <TaskNode
                key={`${subTask.id ?? `${task.id ?? task.name}-${subTask.name}`}-${index}`}
                task={subTask}
                projectId={projectId}
                depth={depth + 1}
                nodeKey={`${nodeKey}.${index}`}
                collapsedTaskKeys={collapsedTaskKeys}
                onToggleTask={onToggleTask}
              />
            ))}
          </div>
        </div>
      ) : null}
    </div>
  )
}

export function TaskTree({ tasks, projectId }: TaskTreeProps) {
  const [search, setSearch] = useState('')
  const [phaseFilter, setPhaseFilter] = useState('ALL')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')
  const [collapsedTaskKeys, setCollapsedTaskKeys] = useState<Set<string>>(new Set())

  const query = search.trim().toLowerCase()
  const phaseOptions = useMemo(
    () =>
      Array.from(new Set(flattenTasks(tasks).map(({ task }) => task.phaseName || 'Bez faze'))).sort((a, b) =>
        a.localeCompare(b, 'sr'),
      ),
    [tasks],
  )
  const filteredTree = useMemo(
    () => filterTaskTree(tasks, query, phaseFilter, dateFrom, dateTo),
    [tasks, query, phaseFilter, dateFrom, dateTo],
  )
  const filteredCount = useMemo(() => countAllTasks(filteredTree), [filteredTree])

  function toggleTask(nodeKey: string) {
    setCollapsedTaskKeys((prev) => {
      const next = new Set(prev)
      if (next.has(nodeKey)) next.delete(nodeKey)
      else next.add(nodeKey)
      return next
    })
  }

  if (tasks.length === 0) {
    return <p className="m-0 text-sm text-ink-subtle">Još nema zadataka na projektu.</p>
  }

  return (
    <div className="space-y-4">
      <div className="grid gap-3 rounded-lg border border-hairline bg-surface-2 p-3 md:grid-cols-4">
        <label className="grid gap-1 md:col-span-2">
          <span className="text-[11px] font-medium tracking-wide text-ink-muted uppercase">Pretraga</span>
          <input
            type="text"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Naziv, opis ili faza..."
            className="min-h-10 rounded-md border border-hairline bg-surface-1 px-3 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
          />
        </label>

        <label className="grid gap-1">
          <span className="text-[11px] font-medium tracking-wide text-ink-muted uppercase">Faza</span>
          <select
            value={phaseFilter}
            onChange={(event) => setPhaseFilter(event.target.value)}
            className="min-h-10 rounded-md border border-hairline bg-surface-1 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
          >
            <option value="ALL">Sve faze</option>
            {phaseOptions.map((phase) => (
              <option key={phase} value={phase}>
                {phase}
              </option>
            ))}
          </select>
        </label>

        <label className="grid gap-1">
          <span className="text-[11px] font-medium tracking-wide text-ink-muted uppercase">Datum od</span>
          <input
            type="date"
            value={dateFrom}
            onChange={(event) => setDateFrom(event.target.value)}
            className="min-h-10 rounded-md border border-hairline bg-surface-1 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
          />
        </label>

        <label className="grid gap-1">
          <span className="text-[11px] font-medium tracking-wide text-ink-muted uppercase">Datum do</span>
          <input
            type="date"
            value={dateTo}
            onChange={(event) => setDateTo(event.target.value)}
            className="min-h-10 rounded-md border border-hairline bg-surface-1 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
          />
        </label>
      </div>

      <div className="flex items-center justify-between text-xs text-ink-subtle">
        <span>Prikazano zadataka: {filteredCount}</span>
        {(query || phaseFilter !== 'ALL' || dateFrom || dateTo) && filteredCount !== countAllTasks(tasks) ? (
          <span>Filteri su aktivni</span>
        ) : null}
      </div>

      {filteredCount === 0 ? (
        <p className="m-0 rounded-md border border-hairline bg-surface-2 px-3 py-3 text-sm text-ink-subtle">
          Nijedan zadatak ne odgovara izabranim filterima.
        </p>
      ) : null}

      {filteredCount > 0 ? (
        <div className="task-thread">
          {filteredTree.map((task, index) => (
            <TaskNode
              key={`${task.id ?? task.name}-${index}`}
              task={task}
              projectId={projectId}
              depth={0}
              nodeKey={`root.${index}`}
              collapsedTaskKeys={collapsedTaskKeys}
              onToggleTask={toggleTask}
            />
          ))}
        </div>
      ) : null}
    </div>
  )
}
