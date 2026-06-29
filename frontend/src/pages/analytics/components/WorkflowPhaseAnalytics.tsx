import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { formatDate } from '../../../lib/formatDate'
import { formatDurationHours } from '../../../lib/formatDuration'
import type { AnalyticsFilters, ProjectWorkflowAnalysis } from '../../../types/analytics'
import type { TaskSummary } from '../../../types/task'
import { getTasksForPhase, phaseSectionKey } from '../lib/workflowTaskUtils'
import { ChevronIcon } from './ChevronIcon'

function DurationSummaryCard({ label, seconds }: { label: string; seconds: number }) {
  return (
    <div className="rounded-lg border border-hairline bg-surface-1 px-4 py-3">
      <p className="m-0 text-xs uppercase tracking-wide text-ink-muted">{label}</p>
      <p className="m-0 mt-1 text-2xl font-semibold text-ink">
        {formatDurationHours(seconds)}
      </p>
    </div>
  )
}

function SummaryCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-lg border border-hairline bg-surface-1 px-4 py-3">
      <p className="m-0 text-xs uppercase tracking-wide text-ink-muted">{label}</p>
      <p className="m-0 mt-1 text-2xl font-semibold text-ink">{value}</p>
    </div>
  )
}

function PhaseTaskCard({ task, projectId }: { task: TaskSummary; projectId: string }) {
  const content = (
    <>
      <h4 className="m-0 text-sm font-medium text-ink">{task.name}</h4>
      <p className="m-0 mt-1 text-xs text-ink-muted">
        {task.description?.trim() || 'Nema opisa'}
      </p>
      <p className="m-0 mt-2 text-xs text-ink-subtle">Rok: {formatDate(task.endDate)}</p>
    </>
  )

  if (!task.id) {
    return (
      <div className="rounded-lg border border-hairline bg-surface-2 px-4 py-3">{content}</div>
    )
  }

  return (
    <Link
      to={`/projects/${projectId}/tasks/${task.id}`}
      className="block rounded-lg border border-hairline bg-surface-2 px-4 py-3 transition-colors hover:border-hairline-strong hover:bg-surface-3"
    >
      {content}
    </Link>
  )
}

interface WorkflowPhaseAnalyticsProps {
  workflow: ProjectWorkflowAnalysis
  projectTasks: TaskSummary[]
  projectId: string
  filters?: Partial<AnalyticsFilters>
}

export function WorkflowPhaseAnalytics({
  workflow,
  projectTasks,
  projectId,
  filters,
}: WorkflowPhaseAnalyticsProps) {
  const phases = useMemo(
    () => [...workflow.phaseAnalytics].sort((a, b) => a.phaseOrder - b.phaseOrder),
    [workflow.phaseAnalytics],
  )
  const duplicatePhaseNames = useMemo(() => {
    const nameCounts = new Map<string, number>()
    for (const phase of phases) {
      nameCounts.set(phase.phaseName, (nameCounts.get(phase.phaseName) ?? 0) + 1)
    }
    return new Set(
      [...nameCounts.entries()].filter(([, count]) => count > 1).map(([name]) => name),
    )
  }, [phases])
  const totalTasks = Math.max(workflow.totalTasks, 1)
  const sectionKeys = useMemo(
    () => phases.map((phase) => phaseSectionKey(phase.phaseId)),
    [phases],
  )
  const [collapsedPhases, setCollapsedPhases] = useState<Set<string>>(() => new Set(sectionKeys))

  useEffect(() => {
    setCollapsedPhases(new Set(sectionKeys))
  }, [sectionKeys])

  function togglePhase(sectionKey: string) {
    setCollapsedPhases((prev) => {
      const next = new Set(prev)
      if (next.has(sectionKey)) next.delete(sectionKey)
      else next.add(sectionKey)
      return next
    })
  }

  return (
    <div className="grid gap-6">
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        <SummaryCard label="Ukupno zadataka" value={workflow.totalTasks} />
        <SummaryCard label="Završeni" value={workflow.completedTasks} />
        <SummaryCard label="Aktivni" value={workflow.activeTasks} />
        <SummaryCard label="Prekoračeni rok" value={workflow.overdueTasks} />
        <DurationSummaryCard
          label="Ukupno trajanje"
          seconds={workflow.totalTaskDurationSeconds}
        />
        <DurationSummaryCard
          label="Prosečno trajanje zadatka"
          seconds={workflow.averageTaskDurationSeconds}
        />
      </div>

      {workflow.taskPhaseHistory.length > 0 && (
        <section className="grid gap-3">
          <h2 className="m-0 text-base font-semibold text-ink">Istorija faza zadatka</h2>
          <div className="overflow-x-auto rounded-lg border border-hairline">
            <table className="min-w-full border-collapse text-sm">
              <thead className="bg-surface-2 text-left text-ink-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">Iz faze</th>
                  <th className="px-4 py-3 font-medium">U fazu</th>
                  <th className="px-4 py-3 font-medium">Trajanje</th>
                  <th className="px-4 py-3 font-medium">Vreme prelaza</th>
                </tr>
              </thead>
              <tbody>
                {workflow.taskPhaseHistory.map((entry, index) => (
                  <tr key={`${entry.transitionedAt}-${index}`} className="border-t border-hairline">
                    <td className="px-4 py-3 text-ink">{entry.fromPhaseName}</td>
                    <td className="px-4 py-3 text-ink">{entry.toPhaseName}</td>
                    <td className="px-4 py-3">{formatDurationHours(entry.durationSeconds)}</td>
                    <td className="px-4 py-3 text-ink-muted">{formatDate(entry.transitionedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {phases.length === 0 ? (
        <p className="m-0 rounded-md border border-hairline bg-surface-1 px-3 py-3 text-sm text-ink-muted">
          Nema zadataka sa dodeljenom fazom za projekat {workflow.projectName}.
        </p>
      ) : (
        <>
          <section className="grid gap-3">
            <h2 className="m-0 text-base font-semibold text-ink">Zadaci po fazama</h2>
            {phases.map((phase) => {
              const sectionKey = phaseSectionKey(phase.phaseId)
              const expanded = !collapsedPhases.has(sectionKey)
              const phaseTasks = getTasksForPhase(
                projectTasks,
                phase.phaseId,
                phase.phaseName,
                phase.phaseOrder,
                filters,
              )
              const phaseLabel = duplicatePhaseNames.has(phase.phaseName)
                ? `${phase.phaseName} (#${phase.phaseOrder})`
                : phase.phaseName

              return (
                <article
                  key={sectionKey}
                  className="overflow-hidden rounded-lg border border-hairline bg-surface-1"
                >
                  <button
                    type="button"
                    onClick={() => togglePhase(sectionKey)}
                    aria-expanded={expanded}
                    className="flex w-full cursor-pointer items-center justify-between gap-3 px-4 py-3 text-left transition-colors hover:bg-surface-2"
                  >
                    <div className="flex min-w-0 items-center gap-2.5">
                      <ChevronIcon expanded={expanded} />
                      <span className="truncate font-medium text-ink">{phaseLabel}</span>
                    </div>
                    <span className="shrink-0 text-sm text-ink-muted">
                      {phase.currentTaskCount}/{totalTasks}
                    </span>
                  </button>

                  <div
                    className={`overflow-hidden border-t border-hairline transition-all duration-300 ease-out ${
                      expanded ? 'max-h-[1200px] opacity-100' : 'max-h-0 opacity-0'
                    }`}
                  >
                    <div className="grid gap-3 px-4 py-3">
                      {phaseTasks.length > 0 ? (
                        phaseTasks.map((task) => (
                          <PhaseTaskCard
                            key={task.id ?? `${task.name}-${task.endDate}`}
                            task={task}
                            projectId={projectId}
                          />
                        ))
                      ) : (
                        <p className="m-0 text-sm text-ink-muted">Nema zadataka u ovoj fazi.</p>
                      )}
                    </div>
                  </div>
                </article>
              )
            })}
          </section>

          <div className="overflow-x-auto rounded-lg border border-hairline">
            <table className="min-w-full border-collapse text-sm">
              <thead className="bg-surface-2 text-left text-ink-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">Redosled</th>
                  <th className="px-4 py-3 font-medium">Faza</th>
                  <th className="px-4 py-3 font-medium">Broj zadataka</th>
                  <th className="px-4 py-3 font-medium">Prosečno vreme u fazi</th>
                </tr>
              </thead>
              <tbody>
                {phases.map((phase) => (
                  <tr key={phase.phaseId} className="border-t border-hairline">
                    <td className="px-4 py-3">{phase.phaseOrder}</td>
                    <td className="px-4 py-3 text-ink">
                      {duplicatePhaseNames.has(phase.phaseName)
                        ? `${phase.phaseName} (#${phase.phaseOrder})`
                        : phase.phaseName}
                    </td>
                    <td className="px-4 py-3">{phase.currentTaskCount}</td>
                    <td className="px-4 py-3">{formatDurationHours(phase.averageSecondsInPhase)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}
