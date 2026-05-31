import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import {
  MOCK_PHASE_COLUMNS,
  MOCK_PROJECT_NAME,
  MOCK_TASK_ID,
  type MockPhaseColumn,
  type MockTaskCard,
  type MockTaskStatus,
} from './myTasksMocks'

function ChevronIcon({ expanded }: { expanded: boolean }) {
  return (
    <svg
      viewBox="0 0 20 20"
      aria-hidden
      className={`h-4 w-4 shrink-0 text-ink-subtle transition-transform duration-200 ${expanded ? 'rotate-90' : 'rotate-0'}`}
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

function TaskCard({ task }: { task: MockTaskCard }) {
  return (
    <Link
      to={`/my-tasks/tasks/${MOCK_TASK_ID}`}
      className="block min-w-[220px] flex-1 rounded-lg border border-hairline-strong bg-surface-2 px-4 py-3 transition-colors hover:border-hairline hover:bg-surface-3"
    >
      <h4 className="m-0 text-sm font-medium text-ink">{task.title}</h4>
      <p className="m-0 mt-2 text-xs text-error">Rok završetka: {task.dueDate}</p>
    </Link>
  )
}

function PhaseSection({
  column,
  collapsed,
  onToggle,
}: {
  column: MockPhaseColumn
  collapsed: boolean
  onToggle: () => void
}) {
  const expanded = !collapsed

  return (
    <article className="overflow-hidden rounded-xl border border-hairline bg-surface-1">
      <button
        type="button"
        onClick={onToggle}
        className="flex w-full cursor-pointer items-center justify-between px-4 py-3 text-left transition-colors hover:bg-surface-2"
      >
        <div className="flex items-center gap-2.5">
          <ChevronIcon expanded={expanded} />
          <h2 className="m-0 text-xl font-semibold tracking-tight text-ink">{column.title}</h2>
        </div>
        <span className="rounded-full border border-hairline bg-surface-2 px-3 py-1 text-sm text-ink-subtle">
          {column.tasks.length}
        </span>
      </button>

      <div
        className={`overflow-hidden border-t border-hairline transition-all duration-300 ease-out ${
          expanded ? 'max-h-[800px] opacity-100' : 'max-h-0 opacity-0'
        }`}
      >
        <div className="flex flex-wrap gap-3 px-4 py-3">
          {column.tasks.length > 0 ? (
            column.tasks.map((task, index) => (
              <TaskCard key={`${task.id}-${index}`} task={task} />
            ))
          ) : (
            <p className="m-0 px-1 py-2 text-sm text-ink-subtle">Nema zadataka u ovoj fazi.</p>
          )}
        </div>
      </div>
    </article>
  )
}

export function MyTasksPage() {
  const [projectFilter, setProjectFilter] = useState(MOCK_PROJECT_NAME)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<'ALL' | MockTaskStatus>('ALL')
  const [collapsedPhases, setCollapsedPhases] = useState<Set<MockTaskStatus>>(() => {
    const initial = new Set<MockTaskStatus>()
    MOCK_PHASE_COLUMNS.forEach((column) => {
      if (!column.defaultExpanded) initial.add(column.id)
    })
    return initial
  })

  const visibleColumns = useMemo(() => {
    const q = search.trim().toLowerCase()
    return MOCK_PHASE_COLUMNS.map((column) => {
      if (statusFilter !== 'ALL' && column.id !== statusFilter) {
        return { ...column, tasks: [] }
      }
      const tasks = column.tasks.filter((task) => {
        const projectMatch = projectFilter === MOCK_PROJECT_NAME
        const searchMatch =
          !q ||
          task.title.toLowerCase().includes(q) ||
          task.projectName.toLowerCase().includes(q) ||
          column.title.toLowerCase().includes(q)
        return projectMatch && searchMatch
      })
      return { ...column, tasks }
    }).filter((column) => statusFilter === 'ALL' || column.id === statusFilter)
  }, [search, projectFilter, statusFilter])

  function togglePhase(phaseId: MockTaskStatus) {
    setCollapsedPhases((prev) => {
      const next = new Set(prev)
      if (next.has(phaseId)) next.delete(phaseId)
      else next.add(phaseId)
      return next
    })
  }

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header>
          <h1 className="m-0 text-3xl font-semibold tracking-tight text-ink">Moji zadaci</h1>
        </header>

        <section className="rounded-xl border border-hairline bg-surface-1 p-4 md:p-5">
          <div className="grid gap-4 md:grid-cols-3">
            <label className="grid gap-1">
              <span className="text-[13px] font-medium tracking-wide text-ink-muted uppercase">Projekat</span>
              <select
                value={projectFilter}
                onChange={(event) => setProjectFilter(event.target.value)}
                className="min-h-11 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
              >
                <option value={MOCK_PROJECT_NAME}>{MOCK_PROJECT_NAME}</option>
              </select>
            </label>

            <label className="grid gap-1">
              <span className="text-[13px] font-medium tracking-wide text-ink-muted uppercase">
                Pretraga zadataka
              </span>
              <input
                type="text"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Unesite naziv/opis zadatka..."
                className="min-h-11 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
              />
            </label>

            <label className="grid gap-1">
              <span className="text-[13px] font-medium tracking-wide text-ink-muted uppercase">Filteri</span>
              <select
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value as 'ALL' | MockTaskStatus)}
                className="min-h-11 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
              >
                <option value="ALL">Svi statusi</option>
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="IN_REVIEW">In Review</option>
                <option value="DONE">Done</option>
              </select>
            </label>
          </div>
        </section>

        <section className="grid gap-4">
          {visibleColumns.map((column) => (
            <PhaseSection
              key={column.id}
              column={column}
              collapsed={collapsedPhases.has(column.id)}
              onToggle={() => togglePhase(column.id)}
            />
          ))}
        </section>
      </div>
    </AppShell>
  )
}
