import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { formatDate } from '../../lib/formatDate'
import type { AssignedTaskSummary } from '../../types/task'
import { useMyTasksPage } from './hooks/useMyTasksPage'
import { groupTasksByPhase, type PhaseColumn } from './utils/groupTasksByPhase'

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

function TaskCard({ task }: { task: AssignedTaskSummary }) {
  if (!task.id) return null

  return (
    <Link
      to={`/my-tasks/tasks/${task.id}`}
      className="block min-w-[220px] flex-1 rounded-lg border border-hairline-strong bg-surface-2 px-4 py-3 transition-colors hover:border-hairline hover:bg-surface-3"
    >
      <h4 className="m-0 text-sm font-medium text-ink">{task.name}</h4>
      {task.description?.trim() ? (
        <p className="m-0 mt-1 line-clamp-2 text-xs text-ink-subtle">{task.description}</p>
      ) : null}
      <p className="m-0 mt-2 text-xs text-error">Rok završetka: {formatDate(task.endDate)}</p>
    </Link>
  )
}

function PhaseSection({
  column,
  collapsed,
  onToggle,
}: {
  column: PhaseColumn
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
            column.tasks.map((task) => <TaskCard key={task.id} task={task} />)
          ) : (
            <p className="m-0 px-1 py-2 text-sm text-ink-subtle">Nema zadataka u ovoj fazi.</p>
          )}
        </div>
      </div>
    </article>
  )
}

export function MyTasksPage() {
  const { projects, selectedProjectId, setSelectedProjectId, tasks, loading, loadingTasks, error } =
    useMyTasksPage()

  const [search, setSearch] = useState('')
  const [phaseFilter, setPhaseFilter] = useState<'ALL' | string>('ALL')
  const [collapsedPhases, setCollapsedPhases] = useState<Set<string>>(new Set())

  const phaseColumns = useMemo(() => groupTasksByPhase(tasks), [tasks])

  const phaseOptions = useMemo(
    () => phaseColumns.map((column) => column.title),
    [phaseColumns],
  )

  const visibleColumns = useMemo(() => {
    const q = search.trim().toLowerCase()
    return phaseColumns
      .map((column) => {
        if (phaseFilter !== 'ALL' && column.id !== phaseFilter) {
          return { ...column, tasks: [] }
        }
        const filteredTasks = column.tasks.filter((task) => {
          if (!q) return true
          return (
            task.name.toLowerCase().includes(q) ||
            (task.description?.toLowerCase().includes(q) ?? false) ||
            column.title.toLowerCase().includes(q)
          )
        })
        return { ...column, tasks: filteredTasks }
      })
      .filter((column) => phaseFilter === 'ALL' || column.id === phaseFilter)
  }, [phaseColumns, phaseFilter, search])

  function togglePhase(phaseId: string) {
    setCollapsedPhases((prev) => {
      const next = new Set(prev)
      if (next.has(phaseId)) next.delete(phaseId)
      else next.add(phaseId)
      return next
    })
  }

  const isLoading = loading || loadingTasks

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header>
          <h1 className="m-0 text-3xl font-semibold tracking-tight text-ink">Moji zadaci</h1>
          <p className="m-0 mt-2 text-sm text-ink-subtle">
            Zadaci dodeljeni vama, grupisani po fazama radnog toka.
          </p>
        </header>

        {error ? (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        ) : null}

        <section className="rounded-xl border border-hairline bg-surface-1 p-4 md:p-5">
          <div className="grid gap-4 md:grid-cols-3">
            <label className="grid gap-1">
              <span className="text-[13px] font-medium tracking-wide text-ink-muted uppercase">Projekat</span>
              <select
                value={selectedProjectId}
                onChange={(event) => setSelectedProjectId(event.target.value)}
                disabled={loading || projects.length === 0}
                className="min-h-11 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
              >
                {projects.length === 0 ? (
                  <option value="">Nema dodeljenih projekata</option>
                ) : (
                  projects.map((project) => (
                    <option key={project.id} value={project.id}>
                      {project.name}
                    </option>
                  ))
                )}
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
              <span className="text-[13px] font-medium tracking-wide text-ink-muted uppercase">Faza</span>
              <select
                value={phaseFilter}
                onChange={(event) => setPhaseFilter(event.target.value)}
                className="min-h-11 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
              >
                <option value="ALL">Sve faze</option>
                {phaseOptions.map((phase) => (
                  <option key={phase} value={phase}>
                    {phase}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </section>

        {isLoading ? (
          <section className="rounded-xl border border-hairline bg-surface-1 p-5">
            <p className="m-0 text-sm text-ink-subtle">Učitavanje…</p>
          </section>
        ) : projects.length === 0 ? (
          <section className="rounded-xl border border-hairline bg-surface-1 p-5">
            <p className="m-0 text-sm text-ink-subtle">
              Trenutno nemate dodeljenih zadataka na projektima.
            </p>
          </section>
        ) : visibleColumns.length === 0 ? (
          <section className="rounded-xl border border-hairline bg-surface-1 p-5">
            <p className="m-0 text-sm text-ink-subtle">Nema zadataka za izabrane filtere.</p>
          </section>
        ) : (
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
        )}
      </div>
    </AppShell>
  )
}
