import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { cn } from '../../lib/cn'
import {
  MOCK_TASK_DETAILS,
  MOCK_TASK_ID,
  type MockCriterion,
  type MockSubtask,
  type SubtaskStatus,
} from './myTasksMocks'

type CriteriaFilter = 'ALL' | 'OPEN' | 'DONE'
type SubtaskStatusFilter = 'ALL' | SubtaskStatus
type SubtaskViewMode = 'TREE' | 'STATUS'

interface FlatSubtaskItem {
  subtask: MockSubtask
  depth: number
}

const SUBTASK_STATUS_ORDER: Record<SubtaskStatus, number> = {
  'In Progress': 0,
  'In Review': 1,
  Done: 2,
}

/** Najdublji indeks: 0 = podzadatak, 1–2 = pod-podzadatak */
const MAX_SUBTASK_DEPTH = 2

/** Glavni zadatak se ne prikazuje u listi — samo njegovi podzadaci. */
function subtasksForDisplay(subtasks: MockSubtask[], parentTitle?: string): MockSubtask[] {
  if (subtasks.length !== 1) return subtasks
  const root = subtasks[0]
  if (root.id === 'main' || (parentTitle && root.title === parentTitle)) {
    return root.children ?? []
  }
  return subtasks
}

function normalizeText(value: string): string {
  return value.trim().toLowerCase()
}

function collectSubtaskStatuses(subtasks: MockSubtask[]): SubtaskStatus[] {
  const statuses = new Set<SubtaskStatus>()
  const visit = (nodes: MockSubtask[]) => {
    nodes.forEach((node) => {
      statuses.add(node.status)
      if (node.children?.length) visit(node.children)
    })
  }
  visit(subtasks)
  return Array.from(statuses).sort((a, b) => SUBTASK_STATUS_ORDER[a] - SUBTASK_STATUS_ORDER[b])
}

function subtaskMatches(subtask: MockSubtask, query: string, statusFilter: SubtaskStatusFilter): boolean {
  const statusMatches = statusFilter === 'ALL' || subtask.status === statusFilter
  if (!statusMatches) return false
  if (!query) return true
  return normalizeText(subtask.title).includes(query)
}

function filterSubtaskTree(
  subtasks: MockSubtask[],
  query: string,
  statusFilter: SubtaskStatusFilter,
): MockSubtask[] {
  return subtasks.reduce<MockSubtask[]>((acc, subtask) => {
    const filteredChildren = filterSubtaskTree(subtask.children ?? [], query, statusFilter)
    const currentMatches = subtaskMatches(subtask, query, statusFilter)

    if (currentMatches || filteredChildren.length > 0) {
      acc.push({ ...subtask, children: filteredChildren })
    }
    return acc
  }, [])
}

function flattenSubtasks(subtasks: MockSubtask[], depth = 0): FlatSubtaskItem[] {
  return subtasks.flatMap((subtask) => [
    { subtask, depth },
    ...flattenSubtasks(subtask.children ?? [], depth + 1),
  ])
}

function hierarchyLabel(depth: number): string {
  if (depth <= 0) return 'podzadatak'
  return 'pod-podzadatak'
}

function clampSubtaskDepth(subtasks: MockSubtask[], depth = 0): MockSubtask[] {
  return subtasks.map((subtask) => ({
    ...subtask,
    children:
      depth < MAX_SUBTASK_DEPTH && subtask.children
        ? clampSubtaskDepth(subtask.children, depth + 1)
        : undefined,
  }))
}

function countSubtasks(subtasks: MockSubtask[]): number {
  return subtasks.reduce((total, subtask) => total + 1 + countSubtasks(subtask.children ?? []), 0)
}

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

function StatusBadge({ status }: { status: SubtaskStatus }) {
  const className =
    status === 'Done'
      ? 'border-success/40 bg-success/10 text-[#6ee7a0]'
      : status === 'In Review'
        ? 'border-primary/40 bg-primary/15 text-primary-hover'
        : 'border-primary/35 bg-primary/10 text-primary-hover'

  return (
    <span className={`shrink-0 rounded-full border px-2.5 py-0.5 text-[11px] font-medium ${className}`}>
      {status}
    </span>
  )
}

function SubtaskGroupedRow({ subtask, depth }: FlatSubtaskItem) {
  return (
    <article className="flex items-center justify-between gap-2 rounded-md border border-transparent px-2 py-1.5 hover:border-hairline hover:bg-surface-2">
      <div className="min-w-0">
        <p className="m-0 truncate text-sm font-medium text-ink">{subtask.title}</p>
      </div>
      <p className="m-0 shrink-0 text-[11px] text-ink-muted">{hierarchyLabel(depth)}</p>
    </article>
  )
}

function SubtaskNode({ subtask, depth }: { subtask: MockSubtask; depth: number }) {
  const [expanded, setExpanded] = useState(depth < 2)
  const hasChildren = (subtask.children?.length ?? 0) > 0
  const isTopLevel = depth === 0

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
        <div className="flex min-w-0 flex-1 items-start justify-between gap-3 border-b border-hairline py-2.5">
          <p className={cn('m-0 text-sm text-ink', isTopLevel && 'font-semibold')}>{subtask.title}</p>
          <StatusBadge status={subtask.status} />
        </div>
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
  onToggle,
}: {
  criterion: MockCriterion
  onToggle: () => void
}) {
  return (
    <label className="flex cursor-pointer items-start gap-3 border-b border-hairline py-2.5 last:border-b-0">
      <span className="relative mt-0.5 inline-flex h-5 w-5 shrink-0 items-center justify-center">
        <input type="checkbox" checked={criterion.checked} onChange={onToggle} className="peer sr-only" />
        <span className="inline-flex h-5 w-5 items-center justify-center rounded border border-hairline bg-surface-2 peer-checked:border-primary peer-checked:bg-primary">
          <svg
            viewBox="0 0 16 16"
            aria-hidden
            className={`h-3 w-3 text-on-primary transition-opacity ${criterion.checked ? 'opacity-100' : 'opacity-0'}`}
          >
            <path
              d="M3.5 8.2 6.4 11 12.5 5"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </span>
      </span>
      <span className={`text-sm ${criterion.checked ? 'text-ink-subtle line-through' : 'text-ink'}`}>
        {criterion.label}
      </span>
    </label>
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

export function MyTaskDetailsPage() {
  const { taskId } = useParams<{ taskId: string }>()
  const isMock = taskId === MOCK_TASK_ID
  const task = MOCK_TASK_DETAILS

  const [criteria, setCriteria] = useState(task.criteria)
  const [criteriaFilter, setCriteriaFilter] = useState<CriteriaFilter>('ALL')
  const [problemType, setProblemType] = useState('')
  const [problemDescription, setProblemDescription] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [subtaskSearch, setSubtaskSearch] = useState('')
  const [subtaskStatusFilter, setSubtaskStatusFilter] = useState<SubtaskStatusFilter>('ALL')
  const [subtaskViewMode, setSubtaskViewMode] = useState<SubtaskViewMode>('TREE')
  const [collapsedStatusGroups, setCollapsedStatusGroups] = useState<Set<string>>(new Set())

  const completedCount = useMemo(() => criteria.filter((c) => c.checked).length, [criteria])
  const progress = Math.round((completedCount / criteria.length) * 100)

  const visibleCriteria = useMemo(() => {
    if (criteriaFilter === 'DONE') return criteria.filter((c) => c.checked)
    if (criteriaFilter === 'OPEN') return criteria.filter((c) => !c.checked)
    return criteria
  }, [criteria, criteriaFilter])

  const subtaskQuery = subtaskSearch.trim().toLowerCase()
  const displaySubtasks = useMemo(
    () => subtasksForDisplay(task.subtasks, task.title),
    [task.subtasks, task.title],
  )
  const subtaskStatusOptions = useMemo(() => collectSubtaskStatuses(displaySubtasks), [displaySubtasks])
  const filteredSubtasks = useMemo(
    () => clampSubtaskDepth(filterSubtaskTree(displaySubtasks, subtaskQuery, subtaskStatusFilter)),
    [displaySubtasks, subtaskQuery, subtaskStatusFilter],
  )
  const groupedByStatus = useMemo(() => {
    const groups = flattenSubtasks(filteredSubtasks).reduce<Record<string, FlatSubtaskItem[]>>(
      (acc, item) => {
        const key = item.subtask.status
        if (!acc[key]) acc[key] = []
        acc[key].push(item)
        return acc
      },
      {},
    )
    return Object.entries(groups).sort(
      ([a], [b]) => SUBTASK_STATUS_ORDER[a as SubtaskStatus] - SUBTASK_STATUS_ORDER[b as SubtaskStatus],
    )
  }, [filteredSubtasks])
  const totalSubtaskCount = useMemo(() => countSubtasks(displaySubtasks), [displaySubtasks])
  const visibleSubtaskCount = useMemo(() => countSubtasks(filteredSubtasks), [filteredSubtasks])
  const subtaskFiltersActive =
    Boolean(subtaskQuery) || subtaskStatusFilter !== 'ALL' || subtaskViewMode !== 'TREE'

  function toggleStatusGroup(status: string) {
    setCollapsedStatusGroups((prev) => {
      const next = new Set(prev)
      if (next.has(status)) next.delete(status)
      else next.add(status)
      return next
    })
  }

  if (!isMock) {
    return (
      <AppShell>
        <div className="mx-auto max-w-6xl">
          <p className="m-0 text-sm text-ink-subtle">
            Detalji za ovaj zadatak još nisu dostupni. Dodele zadataka nisu uključene u ovoj verziji.
          </p>
          <Link
            to={`/my-tasks/tasks/${MOCK_TASK_ID}`}
            className="mt-4 inline-block text-sm text-primary-hover hover:underline"
          >
            Otvori demo zadatak (Implementacija Backenda)
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
          <h1 className="m-0 mt-2 text-3xl font-semibold tracking-tight text-ink md:text-4xl">{task.title}</h1>
        </header>

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
                      onToggle={() =>
                        setCriteria((prev) =>
                          prev.map((item) =>
                            item.id === criterion.id ? { ...item, checked: !item.checked } : item,
                          ),
                        )
                      }
                    />
                  ))
                ) : (
                  <p className="m-0 py-3 text-sm text-ink-subtle">Nema uslova za izabrani filter.</p>
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
                <div className="mt-3 grid gap-2 sm:grid-cols-3">
                  <label className="grid gap-1 sm:col-span-1">
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
                      onChange={(event) => setSubtaskStatusFilter(event.target.value as SubtaskStatusFilter)}
                      className="min-h-9 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                    >
                      <option value="ALL">Svi statusi</option>
                      {subtaskStatusOptions.map((status) => (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="grid gap-1">
                    <span className="text-[11px] font-medium tracking-wide text-ink-muted uppercase">Prikaz</span>
                    <select
                      value={subtaskViewMode}
                      onChange={(event) => setSubtaskViewMode(event.target.value as SubtaskViewMode)}
                      className="min-h-9 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                    >
                      <option value="TREE">Hijerarhijski</option>
                      <option value="STATUS">Grupisano po fazi</option>
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
                    Nijedan podzadatak ne odgovara izabranim filterima.
                  </p>
                ) : null}

                {visibleSubtaskCount > 0 && subtaskViewMode === 'TREE' ? (
                  <div className="task-thread">
                    {filteredSubtasks.map((subtask) => (
                      <SubtaskNode key={subtask.id} subtask={subtask} depth={0} />
                    ))}
                  </div>
                ) : null}

                {visibleSubtaskCount > 0 && subtaskViewMode === 'STATUS' ? (
                  <div className="space-y-2">
                    {groupedByStatus.map(([status, items]) => (
                      <article
                        key={status}
                        className="overflow-hidden rounded-lg border border-hairline bg-surface-2"
                      >
                        <button
                          type="button"
                          onClick={() => toggleStatusGroup(status)}
                          className="flex w-full items-center justify-between px-3 py-2 text-left hover:bg-surface-3"
                        >
                          <div className="flex items-center gap-2">
                            <ChevronIcon expanded={!collapsedStatusGroups.has(status)} />
                            <StatusBadge status={status as SubtaskStatus} />
                          </div>
                          <span className="rounded-full border border-hairline px-2 py-0.5 text-[11px] text-ink-muted">
                            {items.length}
                          </span>
                        </button>
                        <div
                          className={`overflow-hidden transition-all duration-300 ease-out ${
                            !collapsedStatusGroups.has(status)
                              ? 'max-h-[2000px] opacity-100'
                              : 'max-h-0 opacity-0'
                          }`}
                        >
                          <div className="space-y-0.5 border-t border-hairline p-1">
                            {items.map((item) => (
                              <SubtaskGroupedRow
                                key={`${status}-${item.subtask.id}-${item.depth}`}
                                subtask={item.subtask}
                                depth={item.depth}
                              />
                            ))}
                          </div>
                        </div>
                      </article>
                    ))}
                  </div>
                ) : null}
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
                      <StatusBadge status={task.phase} />
                    </dd>
                  </div>
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Dodeljeno</dt>
                    <dd className="m-0 mt-1 text-sm text-ink">{task.assignees}</dd>
                  </div>
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Rok</dt>
                    <dd className="m-0 mt-1 text-sm text-error">{task.dueDate}</dd>
                  </div>
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Radni tok</dt>
                    <dd className="m-0 mt-1 text-sm text-ink">{task.workflow}</dd>
                  </div>
                </dl>
              </div>

              <div className="border-b border-hairline py-4">
                <h2 className="m-0 text-base font-semibold text-ink">Napredak razvoja</h2>
                <dl className="m-0 mt-3 grid gap-3">
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Provedeno vreme</dt>
                    <dd className="m-0 mt-1 text-sm text-ink">{task.spentTime}</dd>
                  </div>
                  <div>
                    <dt className="m-0 text-xs text-ink-subtle">Broj ispunjenih uslova</dt>
                    <dd className="m-0 mt-1 text-sm text-ink">
                      {progress}% ({completedCount}/{criteria.length}) uslova
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
                      onChange={(event) => setProblemType(event.target.value)}
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
                  {submitted ? (
                    <p className="m-0 text-xs text-ink-subtle">Demo: prijava je sačuvana lokalno (bez API poziva).</p>
                  ) : null}
                  <Button
                    onClick={() => setSubmitted(true)}
                    disabled={!problemType || !problemDescription.trim()}
                  >
                    Pošalji prijavu
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
