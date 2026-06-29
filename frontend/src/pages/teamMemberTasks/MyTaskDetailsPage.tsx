import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchTaskById, fetchTaskTransitions, toggleAcceptanceCriterion } from '../../api/tasks'
import { reportProblem, type ProblemType } from '../../api/taskProblems'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { cn } from '../../lib/cn'
import { formatDate } from '../../lib/formatDate'
import type { ProjectTask, TaskTransitionsResponse } from '../../types/task'
import { TaskPhaseBoard } from './components/TaskPhaseBoard'
import {
  mapCriteria,
  mapSubtask,
  type DisplayCriterion,
  type DisplaySubtask,
} from './utils/taskDetailsMappers'

type CriteriaFilter = 'ALL' | 'OPEN' | 'DONE'
type SubtaskStatusFilter = 'ALL' | string

function ChevronIcon({ expanded }: { expanded: boolean }) {
  return (
    <svg
      viewBox="0 0 20 20"
      aria-hidden
      className={`h-4 w-4 text-ink-subtle transition-transform duration-200 ${expanded ? 'rotate-90' : 'rotate-0'}`}
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

function PhaseBadge({ status }: { status: string }) {
  return (
    <span className="shrink-0 rounded-full border border-primary/35 bg-primary/10 px-2.5 py-0.5 text-[11px] font-medium text-primary-hover">
      {status}
    </span>
  )
}

function SubtaskNode({ subtask, depth }: { subtask: DisplaySubtask; depth: number }) {
  const [expanded, setExpanded] = useState(depth < 2)
  const hasChildren = (subtask.children?.length ?? 0) > 0
  const isTopLevel = depth === 0
  const subtaskHref = subtask.id ? `/my-tasks/tasks/${subtask.id}` : undefined

  const rowContent = (
    <div className="flex min-w-0 flex-1 items-start justify-between gap-3 border-b border-hairline py-2.5">
      <p
        className={cn(
          'm-0 text-sm text-ink transition-colors group-hover:text-primary-hover',
          isTopLevel && 'font-semibold',
        )}
      >
        {subtask.title}
      </p>
      <PhaseBadge status={subtask.status} />
    </div>
  )

  const rowBody = subtaskHref ? (
    <Link to={subtaskHref} className="group block min-w-0 flex-1">
      {rowContent}
    </Link>
  ) : (
    <div className="min-w-0 flex-1">{rowContent}</div>
  )

  return (
    <div className={isTopLevel ? 'task-thread__root' : 'task-thread__node'}>
      <div className="flex items-start gap-2">
        {hasChildren ? (
          <button
            type="button"
            onClick={() => setExpanded((value) => !value)}
            aria-label={expanded ? 'Sakrij podzadatke' : 'Prikaži podzadatke'}
            className="mt-1 inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-sm hover:bg-surface-3"
          >
            <ChevronIcon expanded={expanded} />
          </button>
        ) : (
          <span className="mt-1 inline-flex h-5 w-5 shrink-0" aria-hidden />
        )}
        {rowBody}
      </div>

      {hasChildren ? (
        <div
          className={`task-thread__collapse overflow-hidden transition-all duration-300 ease-out ${
            expanded ? 'max-h-[2000px] opacity-100' : 'max-h-0 opacity-0'
          }`}
        >
          <div className="task-thread__replies">
            {subtask.children?.map((child) => (
              <SubtaskNode key={child.id} subtask={child} depth={depth + 1} />
            ))}
          </div>
        </div>
      ) : null}
    </div>
  )
}

function CriterionRow({
  criterion,
  toggling,
  onToggle,
}: {
  criterion: DisplayCriterion
  toggling: boolean
  onToggle: (id: string) => void
}) {
  return (
    <div className="flex items-start gap-3 border-b border-hairline py-2.5 last:border-b-0">
      <button
        type="button"
        disabled={toggling || !/^\d+$/.test(criterion.id)}
        onClick={() => onToggle(criterion.id)}
        aria-label={criterion.checked ? 'Označi kao nerešeno' : 'Označi kao rešeno'}
        className="relative mt-0.5 inline-flex h-5 w-5 shrink-0 items-center justify-center disabled:opacity-50"
      >
        <span
          className={cn(
            'inline-flex h-5 w-5 items-center justify-center rounded border',
            criterion.checked
              ? 'border-primary bg-primary'
              : 'border-hairline bg-surface-2',
          )}
        >
          {criterion.checked ? (
            <svg viewBox="0 0 16 16" aria-hidden className="h-3 w-3 text-on-primary">
              <path
                d="M3.5 8.2 6.4 11 12.5 5"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          ) : null}
        </span>
      </button>
      <span className={cn('text-sm', criterion.checked ? 'text-ink-subtle line-through' : 'text-ink')}>
        {criterion.label}
      </span>
    </div>
  )
}

function CriteriaFilterTabs({
  value,
  onChange,
}: {
  value: CriteriaFilter
  onChange: (value: CriteriaFilter) => void
}) {
  const options: { id: CriteriaFilter; label: string }[] = [
    { id: 'ALL', label: 'Svi' },
    { id: 'OPEN', label: 'Nerešeni' },
    { id: 'DONE', label: 'Rešeni' },
  ]

  return (
    <div className="mt-3 flex flex-wrap gap-2">
      {options.map((option) => (
        <button
          key={option.id}
          type="button"
          onClick={() => onChange(option.id)}
          className={cn(
            'rounded-md border px-2.5 py-1 text-xs transition-colors',
            value === option.id
              ? 'border-primary/40 bg-primary/15 text-primary-hover'
              : 'border-hairline bg-surface-2 text-ink-subtle hover:text-ink',
          )}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}

function normalizeText(value: string): string {
  return value.trim().toLowerCase()
}

function collectSubtaskStatuses(subtasks: DisplaySubtask[]): string[] {
  const statuses = new Set<string>()
  const visit = (nodes: DisplaySubtask[]) => {
    nodes.forEach((node) => {
      statuses.add(node.status)
      if (node.children?.length) visit(node.children)
    })
  }
  visit(subtasks)
  return Array.from(statuses).sort((a, b) => a.localeCompare(b, 'sr'))
}

function subtaskMatches(subtask: DisplaySubtask, query: string, statusFilter: SubtaskStatusFilter): boolean {
  const statusMatches = statusFilter === 'ALL' || subtask.status === statusFilter
  if (!statusMatches) return false
  if (!query) return true
  return normalizeText(subtask.title).includes(query)
}

function filterSubtaskTree(
  subtasks: DisplaySubtask[],
  query: string,
  statusFilter: SubtaskStatusFilter,
): DisplaySubtask[] {
  return subtasks.reduce<DisplaySubtask[]>((acc, subtask) => {
    const filteredChildren = filterSubtaskTree(subtask.children ?? [], query, statusFilter)
    const currentMatches = subtaskMatches(subtask, query, statusFilter)

    if (currentMatches || filteredChildren.length > 0) {
      acc.push({ ...subtask, children: filteredChildren })
    }
    return acc
  }, [])
}

function clampSubtaskDepth(subtasks: DisplaySubtask[], depth = 0): DisplaySubtask[] {
  return subtasks.map((subtask) => ({
    ...subtask,
    children:
      depth < 2 && subtask.children ? clampSubtaskDepth(subtask.children, depth + 1) : undefined,
  }))
}

function countSubtasks(subtasks: DisplaySubtask[]): number {
  return subtasks.reduce((total, subtask) => total + 1 + countSubtasks(subtask.children ?? []), 0)
}

export function MyTaskDetailsPage() {
  const { taskId } = useParams<{ taskId: string }>()
  const [task, setTask] = useState<ProjectTask | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [criteriaFilter, setCriteriaFilter] = useState<CriteriaFilter>('ALL')
  const [problemType, setProblemType] = useState<ProblemType | ''>('')
  const [problemDescription, setProblemDescription] = useState('')
  const [submittingProblem, setSubmittingProblem] = useState(false)
  const [problemMessage, setProblemMessage] = useState<string | null>(null)
  const [subtaskSearch, setSubtaskSearch] = useState('')
  const [subtaskStatusFilter, setSubtaskStatusFilter] = useState<SubtaskStatusFilter>('ALL')
  const [transitionsData, setTransitionsData] = useState<TaskTransitionsResponse | null>(null)
  const [loadingTransitions, setLoadingTransitions] = useState(true)
  const [togglingCriterionId, setTogglingCriterionId] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!taskId) return
    setLoading(true)
    setLoadingTransitions(true)
    setError(null)
    try {
      const [data, transitions] = await Promise.all([
        fetchTaskById(taskId),
        fetchTaskTransitions(taskId),
      ])
      setTask(data)
      setTransitionsData(transitions)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje zadatka nije uspelo')
      setTask(null)
      setTransitionsData(null)
    } finally {
      setLoading(false)
      setLoadingTransitions(false)
    }
  }, [taskId])

  useEffect(() => {
    void load()
  }, [load])

  const criteria = useMemo(() => mapCriteria(task?.acceptanceCriteria), [task?.acceptanceCriteria])
  const displaySubtasks = useMemo(
    () => (task?.subTasks ?? []).map(mapSubtask),
    [task?.subTasks],
  )

  const completedCount = useMemo(() => criteria.filter((c) => c.checked).length, [criteria])
  const progress = criteria.length > 0 ? Math.round((completedCount / criteria.length) * 100) : 0

  const visibleCriteria = useMemo(() => {
    if (criteriaFilter === 'DONE') return criteria.filter((c) => c.checked)
    if (criteriaFilter === 'OPEN') return criteria.filter((c) => !c.checked)
    return criteria
  }, [criteria, criteriaFilter])

  const subtaskQuery = subtaskSearch.trim().toLowerCase()
  const subtaskStatusOptions = useMemo(() => collectSubtaskStatuses(displaySubtasks), [displaySubtasks])
  const filteredSubtasks = useMemo(
    () => clampSubtaskDepth(filterSubtaskTree(displaySubtasks, subtaskQuery, subtaskStatusFilter)),
    [displaySubtasks, subtaskQuery, subtaskStatusFilter],
  )
  const totalSubtaskCount = useMemo(() => countSubtasks(displaySubtasks), [displaySubtasks])
  const visibleSubtaskCount = useMemo(() => countSubtasks(filteredSubtasks), [filteredSubtasks])
  const subtaskFiltersActive = Boolean(subtaskQuery) || subtaskStatusFilter !== 'ALL'

  async function handleToggleCriterion(criterionId: string) {
    setTogglingCriterionId(criterionId)
    try {
      await toggleAcceptanceCriterion(criterionId)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ažuriranje kriterijuma nije uspelo')
    } finally {
      setTogglingCriterionId(null)
    }
  }

  async function handleReportProblem() {
    if (!task?.id || !problemType || !problemDescription.trim()) return
    setSubmittingProblem(true)
    setProblemMessage(null)
    try {
      await reportProblem({
        taskId: task.id,
        problemType,
        description: problemDescription.trim(),
      })
      setProblemType('')
      setProblemDescription('')
      setProblemMessage('Prijava problema je uspešno poslata.')
    } catch (err) {
      setProblemMessage(err instanceof Error ? err.message : 'Slanje prijave nije uspelo')
    } finally {
      setSubmittingProblem(false)
    }
  }

  if (loading) {
    return (
      <AppShell>
        <div className="mx-auto max-w-6xl">
          <p className="m-0 text-sm text-ink-subtle">Učitavanje...</p>
        </div>
      </AppShell>
    )
  }

  if (error || !task) {
    return (
      <AppShell>
        <div className="mx-auto max-w-6xl">
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error ?? 'Zadatak nije pronađen.'}
          </p>
          <Link to="/my-tasks" className="mt-4 inline-block text-sm text-primary-hover hover:underline">
            Nazad na zadatke
          </Link>
        </div>
      </AppShell>
    )
  }

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header>
          <Link to="/my-tasks" className="text-sm text-ink-subtle hover:text-primary-hover hover:underline">
            Nazad na zadatke
          </Link>
          <h1 className="m-0 mt-2 text-3xl font-semibold tracking-tight text-ink md:text-4xl">{task.name}</h1>
          {task.description?.trim() ? (
            <p className="m-0 mt-2 text-sm text-ink-subtle">{task.description}</p>
          ) : null}
        </header>

        <TaskPhaseBoard
          taskId={taskId!}
          transitionsData={transitionsData}
          loading={loadingTransitions}
          onMoved={() => void load()}
        />

        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_300px] lg:items-start">
          <div className="flex h-[600px] flex-col gap-4">
            <section className="flex max-h-[350px] min-h-[240px] shrink-0 flex-col overflow-hidden rounded-xl border border-hairline bg-surface-1 p-4">
              <div className="shrink-0 border-b border-hairline pb-3">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <h2 className="m-0 text-lg font-semibold text-ink">Uslovi realizacije</h2>
                  <span className="text-sm text-ink-subtle">
                    {completedCount}/{criteria.length} ispunjeno
                  </span>
                </div>
                <CriteriaFilterTabs value={criteriaFilter} onChange={setCriteriaFilter} />
              </div>
              <div className="scrollbar-dark mt-1 min-h-0 flex-1 overflow-y-auto pr-1">
                {visibleCriteria.length > 0 ? (
                  visibleCriteria.map((criterion) => (
                    <CriterionRow
                      key={criterion.id}
                      criterion={criterion}
                      toggling={togglingCriterionId === criterion.id}
                      onToggle={(id) => void handleToggleCriterion(id)}
                    />
                  ))
                ) : (
                  <p className="m-0 py-3 text-sm text-ink-subtle">
                    {criteria.length === 0
                      ? 'Nema definisanih uslova realizacije.'
                      : 'Nema uslova za izabrani filter.'}
                  </p>
                )}
              </div>
            </section>

            <section className="flex min-h-[410px] flex-1 flex-col overflow-hidden rounded-xl border border-hairline bg-surface-1 p-4">
              <div className="shrink-0 border-b border-hairline pb-3">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <h2 className="m-0 text-lg font-semibold text-ink">Podzadaci</h2>
                  <span className="text-xs text-ink-subtle">
                    Prikazano: {visibleSubtaskCount}
                    {subtaskFiltersActive ? ` / ${totalSubtaskCount}` : null}
                  </span>
                </div>
                <div className="mt-3 grid gap-2 sm:grid-cols-2">
                  <label className="grid gap-1">
                    <span className="text-[11px] font-medium tracking-wide text-ink-muted uppercase">
                      Pretraga
                    </span>
                    <input
                      type="text"
                      value={subtaskSearch}
                      onChange={(event) => setSubtaskSearch(event.target.value)}
                      placeholder="Naziv podzadatka..."
                      className="min-h-9 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                    />
                  </label>
                  <label className="grid gap-1">
                    <span className="text-[11px] font-medium tracking-wide text-ink-muted uppercase">Faza</span>
                    <select
                      value={subtaskStatusFilter}
                      onChange={(event) => setSubtaskStatusFilter(event.target.value)}
                      className="min-h-9 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                    >
                      <option value="ALL">Sve faze</option>
                      {subtaskStatusOptions.map((status) => (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
                {subtaskFiltersActive ? (
                  <p className="m-0 mt-2 text-xs text-ink-subtle">Filteri su aktivni</p>
                ) : null}
              </div>
              <div className="scrollbar-dark mt-2 min-h-0 flex-1 overflow-y-auto overscroll-contain pr-1">
                {visibleSubtaskCount === 0 ? (
                  <p className="m-0 py-3 text-sm text-ink-subtle">
                    {displaySubtasks.length === 0
                      ? 'Ovaj zadatak nema podzadataka.'
                      : 'Nijedan podzadatak ne odgovara izabranim filterima.'}
                  </p>
                ) : (
                  <div className="task-thread">
                    {filteredSubtasks.map((subtask) => (
                      <SubtaskNode key={subtask.id} subtask={subtask} depth={0} />
                    ))}
                  </div>
                )}
              </div>
            </section>
          </div>

          <aside className="self-start lg:sticky lg:top-20">
            <section className="rounded-xl border border-hairline bg-surface-1 p-4">
              <div className="border-b border-hairline pb-4">
                <h2 className="m-0 text-base font-semibold text-ink">Informacije</h2>
                <dl className="m-0 mt-3 grid gap-3">
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Faza</dt>
                    <dd className="m-0 mt-1">
                      <PhaseBadge status={task.phaseName ?? 'Bez faze'} />
                    </dd>
                  </div>
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Rok</dt>
                    <dd className="m-0 mt-1 text-sm text-error">{formatDate(task.endDate)}</dd>
                  </div>
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Radni tok</dt>
                    <dd className="m-0 mt-1 text-sm text-ink">{task.workflow?.name ?? 'Nije dodeljen'}</dd>
                  </div>
                </dl>
              </div>

              <div className="border-b border-hairline py-4">
                <h2 className="m-0 text-base font-semibold text-ink">Napredak</h2>
                <dl className="m-0 mt-3 grid gap-3">
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Broj ispunjenih uslova</dt>
                    <dd className="m-0 mt-1 text-sm text-ink">
                      {progress}% ({completedCount}/{criteria.length})
                    </dd>
                  </div>
                </dl>
                <div className="mt-3 h-2 overflow-hidden rounded-full bg-surface-3">
                  <div
                    className="h-full rounded-full bg-primary transition-all duration-300"
                    style={{ width: `${progress}%` }}
                  />
                </div>
              </div>

              <div className="pt-4">
                <h2 className="m-0 text-base font-semibold text-ink">Prijava problema</h2>
                <div className="mt-3 grid gap-3">
                  <label className="grid gap-1">
                    <span className="text-xs text-ink-subtle">Tip problema</span>
                    <select
                      value={problemType}
                      onChange={(event) => setProblemType(event.target.value as ProblemType | '')}
                      className="min-h-10 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                    >
                      <option value="">Izaberite tip problema...</option>
                      <option value="TECHNICAL">Tehnički</option>
                      <option value="TEAM">Timski</option>
                      <option value="OTHER">Ostalo</option>
                    </select>
                  </label>
                  <label className="grid gap-1">
                    <span className="text-xs text-ink-subtle">Opis problema</span>
                    <textarea
                      value={problemDescription}
                      onChange={(event) => setProblemDescription(event.target.value)}
                      placeholder="Unesite opis problema..."
                      className="min-h-28 resize-y rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                    />
                  </label>
                  {problemMessage ? (
                    <p className="m-0 text-xs text-ink-subtle">{problemMessage}</p>
                  ) : null}
                  <Button
                    onClick={() => void handleReportProblem()}
                    disabled={submittingProblem || !problemType || !problemDescription.trim()}
                  >
                    {submittingProblem ? 'Slanje...' : 'Pošalji prijavu'}
                  </Button>
                </div>
              </div>
            </section>
          </aside>
        </div>
      </div>
    </AppShell>
  )
}
