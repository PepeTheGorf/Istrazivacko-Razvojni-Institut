import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  fetchMyProblemReports,
  type ProblemReport,
  type ProblemStatus,
} from '../../api/taskProblems'
import { AppShell } from '../../components/layout/AppShell'
import { SelectField } from '../../components/ui/SelectField'
import { cn } from '../../lib/cn'
import { formatDate } from '../../lib/formatDate'

const STATUS_LABELS: Record<ProblemStatus, string> = {
  OPEN: 'Otvoren',
  IN_PROGRESS: 'U obradi',
  RESOLVED: 'Rešen',
}

const PROBLEM_TYPE_LABELS = {
  TECHNICAL: 'Tehnički',
  TEAM: 'Timski',
  OTHER: 'Ostalo',
} as const

type StatusFilter = 'ALL' | ProblemStatus

function statusBadgeClass(status?: ProblemStatus): string {
  if (status === 'RESOLVED') return 'border-semantic-success/35 bg-semantic-success/10 text-semantic-success'
  if (status === 'IN_PROGRESS') return 'border-primary/35 bg-primary/10 text-primary-hover'
  return 'border-hairline bg-surface-2 text-ink-subtle'
}

export function MyReportedProblemsPage() {
  const [problems, setProblems] = useState<ProblemReport[]>([])
  const [selectedTaskId, setSelectedTaskId] = useState('ALL')
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadProblems = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setProblems(await fetchMyProblemReports())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje prijava nije uspelo')
      setProblems([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadProblems()
  }, [loadProblems])

  const taskOptions = useMemo(() => {
    const byId = new Map<string, string>()
    problems.forEach((problem) => {
      if (problem.taskId != null) {
        byId.set(String(problem.taskId), problem.taskName ?? `Zadatak #${problem.taskId}`)
      }
    })
    return Array.from(byId.entries())
      .map(([id, name]) => ({ id, name }))
      .sort((a, b) => a.name.localeCompare(b.name, 'sr'))
  }, [problems])

  const filteredProblems = useMemo(() => {
    const q = search.trim().toLowerCase()
    return problems.filter((problem) => {
      const taskMatches = selectedTaskId === 'ALL' || String(problem.taskId) === selectedTaskId
      const statusMatches = statusFilter === 'ALL' || problem.status === statusFilter
      if (!taskMatches || !statusMatches) return false
      if (!q) return true
      return (
        problem.description?.toLowerCase().includes(q) ||
        problem.taskName?.toLowerCase().includes(q) ||
        (problem.problemType && PROBLEM_TYPE_LABELS[problem.problemType].toLowerCase().includes(q))
      )
    })
  }, [problems, search, selectedTaskId, statusFilter])

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header>
          <p className="m-0 mb-2 text-[13px] font-medium tracking-wide text-ink-subtle uppercase">
            Problemi
          </p>
          <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
            Prijavljeni problemi
          </h1>
          <p className="m-0 mt-2 text-sm text-ink-subtle">
            Pregledajte svoje prijave po zadatku i pratite da li su rešene.
          </p>
        </header>

        <section className="grid gap-3 md:grid-cols-[minmax(0,1fr)_200px_220px]">
          <label className="grid gap-1">
            <span className="text-[13px] font-medium text-ink-muted">Pretraga</span>
            <input
              type="text"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Opis, tip ili zadatak..."
              className="min-h-10 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
            />
          </label>
          <SelectField
            label="Zadatak"
            name="taskId"
            value={selectedTaskId}
            onChange={(event) => setSelectedTaskId(event.target.value)}
          >
            <option value="ALL">Svi zadaci</option>
            {taskOptions.map((task) => (
              <option key={task.id} value={task.id}>
                {task.name}
              </option>
            ))}
          </SelectField>
          <SelectField
            label="Status"
            name="statusFilter"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as StatusFilter)}
          >
            <option value="ALL">Svi statusi</option>
            <option value="OPEN">Otvoren</option>
            <option value="IN_PROGRESS">U obradi</option>
            <option value="RESOLVED">Rešen</option>
          </SelectField>
        </section>

        {error ? (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        ) : null}

        {loading ? (
          <p className="m-0 text-sm text-ink-subtle">Učitavanje prijava...</p>
        ) : filteredProblems.length === 0 ? (
          <section className="rounded-xl border border-hairline bg-surface-1 p-6 text-center">
            <p className="m-0 text-sm text-ink-subtle">
              {problems.length === 0
                ? 'Još niste prijavili nijedan problem.'
                : 'Nema prijava za izabrane filtere.'}
            </p>
          </section>
        ) : (
          <div className="grid gap-3">
            {filteredProblems.map((problem) => (
              <article
                key={problem.id}
                className="rounded-xl border border-hairline bg-surface-1 p-4"
              >
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span
                        className={cn(
                          'rounded-full border px-2 py-0.5 text-[11px] font-medium',
                          statusBadgeClass(problem.status),
                        )}
                      >
                        {problem.status ? STATUS_LABELS[problem.status] : 'Nepoznat'}
                      </span>
                      {problem.problemType ? (
                        <span className="text-xs text-ink-subtle">
                          {PROBLEM_TYPE_LABELS[problem.problemType]}
                        </span>
                      ) : null}
                    </div>
                    {problem.taskId ? (
                      <Link
                        to={`/my-tasks/tasks/${problem.taskId}`}
                        className="m-0 mt-2 inline-block text-sm font-medium text-primary-hover hover:underline"
                      >
                        {problem.taskName ?? `Zadatak #${problem.taskId}`}
                      </Link>
                    ) : null}
                    <p className="m-0 mt-2 text-sm text-ink">{problem.description}</p>
                    <p className="m-0 mt-2 text-xs text-ink-tertiary">
                      Prijavljeno: {formatDate(problem.reportedAt)}
                    </p>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
