import { useCallback, useEffect, useMemo, useState } from 'react'
import { fetchProjects } from '../../api/projects'
import { fetchTasksByProject } from '../../api/tasks'
import {
  fetchProblemsByTask,
  updateProblemReport,
  type ProblemReport,
  type ProblemStatus,
} from '../../api/taskProblems'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { SelectField } from '../../components/ui/SelectField'
import { cn } from '../../lib/cn'
import { formatDate } from '../../lib/formatDate'
import type { Project } from '../../types/project'
import type { TaskSummary } from '../../types/task'

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

export function ReportedProblemsPage() {
  const [projects, setProjects] = useState<Project[]>([])
  const [tasks, setTasks] = useState<TaskSummary[]>([])
  const [problems, setProblems] = useState<ProblemReport[]>([])
  const [selectedProjectId, setSelectedProjectId] = useState('')
  const [selectedTaskId, setSelectedTaskId] = useState('')
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [loadingProjects, setLoadingProjects] = useState(true)
  const [loadingTasks, setLoadingTasks] = useState(false)
  const [loadingProblems, setLoadingProblems] = useState(false)
  const [updatingId, setUpdatingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoadingProjects(true)
    void fetchProjects()
      .then(setProjects)
      .catch((err) => {
        setError(err instanceof Error ? err.message : 'Učitavanje projekata nije uspelo')
      })
      .finally(() => setLoadingProjects(false))
  }, [])

  const loadTasks = useCallback(async (projectId: string) => {
    if (!projectId) {
      setTasks([])
      return
    }
    setLoadingTasks(true)
    setError(null)
    try {
      setTasks(await fetchTasksByProject(projectId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje zadataka nije uspelo')
      setTasks([])
    } finally {
      setLoadingTasks(false)
    }
  }, [])

  const loadProblems = useCallback(async (taskId: string) => {
    if (!taskId) {
      setProblems([])
      return
    }
    setLoadingProblems(true)
    setError(null)
    try {
      setProblems(await fetchProblemsByTask(Number(taskId)))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje prijava nije uspelo')
      setProblems([])
    } finally {
      setLoadingProblems(false)
    }
  }, [])

  useEffect(() => {
    void loadTasks(selectedProjectId)
    setSelectedTaskId('')
    setProblems([])
  }, [selectedProjectId, loadTasks])

  useEffect(() => {
    void loadProblems(selectedTaskId)
  }, [selectedTaskId, loadProblems])

  const filteredProblems = useMemo(() => {
    const q = search.trim().toLowerCase()
    return problems.filter((problem) => {
      const statusMatches = statusFilter === 'ALL' || problem.status === statusFilter
      if (!statusMatches) return false
      if (!q) return true
      return (
        problem.description?.toLowerCase().includes(q) ||
        (problem.problemType && PROBLEM_TYPE_LABELS[problem.problemType].toLowerCase().includes(q))
      )
    })
  }, [problems, search, statusFilter])

  async function handleStatusChange(problemId: number, status: ProblemStatus) {
    setUpdatingId(problemId)
    setError(null)
    try {
      await updateProblemReport(problemId, { status })
      await loadProblems(selectedTaskId)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ažuriranje statusa nije uspelo')
    } finally {
      setUpdatingId(null)
    }
  }

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
            Pregledajte prijave po zadatku i ažurirajte njihov status.
          </p>
        </header>

        <section className="grid gap-4 rounded-xl border border-hairline bg-surface-1 p-4 md:grid-cols-2">
          <SelectField
            label="Projekat"
            name="projectId"
            disabled={loadingProjects}
            value={selectedProjectId}
            onChange={(event) => setSelectedProjectId(event.target.value)}
          >
            <option value="">
              {loadingProjects ? 'Učitavanje projekata…' : 'Izaberite projekat'}
            </option>
            {projects.map((project) => (
              <option key={project.id} value={project.id}>
                {project.name}
              </option>
            ))}
          </SelectField>

          <SelectField
            label="Zadatak"
            name="taskId"
            disabled={!selectedProjectId || loadingTasks}
            value={selectedTaskId}
            onChange={(event) => setSelectedTaskId(event.target.value)}
          >
            <option value="">
              {!selectedProjectId
                ? 'Prvo izaberite projekat'
                : loadingTasks
                  ? 'Učitavanje zadataka…'
                  : 'Izaberite zadatak'}
            </option>
            {tasks.map((task) => (
              <option key={task.id} value={task.id}>
                {task.name}
              </option>
            ))}
          </SelectField>
        </section>

        {selectedTaskId ? (
          <>
            <div className="grid gap-3 md:grid-cols-[minmax(0,1fr)_200px]">
              <label className="grid gap-1">
                <span className="text-[13px] font-medium text-ink-muted">Pretraga</span>
                <input
                  type="text"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Opis ili tip problema..."
                  className="min-h-10 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                />
              </label>
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
            </div>

            {error ? (
              <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
                {error}
              </p>
            ) : null}

            {loadingProblems ? (
              <p className="m-0 text-sm text-ink-subtle">Učitavanje prijava…</p>
            ) : filteredProblems.length === 0 ? (
              <section className="rounded-xl border border-hairline bg-surface-1 p-6 text-center">
                <p className="m-0 text-sm text-ink-subtle">
                  {problems.length === 0
                    ? 'Za izabrani zadatak nema prijavljenih problema.'
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
                        <p className="m-0 mt-2 text-sm text-ink">{problem.description}</p>
                        <p className="m-0 mt-2 text-xs text-ink-tertiary">
                          Prijavljeno: {formatDate(problem.reportedAt)}
                        </p>
                      </div>
                      {problem.status !== 'RESOLVED' && problem.id ? (
                        <div className="flex flex-wrap gap-2">
                          {problem.status === 'OPEN' ? (
                            <Button
                              variant="secondary"
                              disabled={updatingId === problem.id}
                              onClick={() => void handleStatusChange(problem.id!, 'IN_PROGRESS')}
                            >
                              U obradi
                            </Button>
                          ) : null}
                          <Button
                            disabled={updatingId === problem.id}
                            onClick={() => void handleStatusChange(problem.id!, 'RESOLVED')}
                          >
                            Reši
                          </Button>
                        </div>
                      ) : null}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </>
        ) : null}
      </div>
    </AppShell>
  )
}
