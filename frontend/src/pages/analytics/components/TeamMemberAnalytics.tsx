import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { formatDate } from '../../../lib/formatDate'
import type { AnalyticsFilters, TaskTeamMemberStats } from '../../../types/analytics'
import type { TaskSummary } from '../../../types/task'
import { exportTeamMemberAnalyticsPdf } from '../lib/exportAnalyticsPdf'
import {
  filterMemberTasksByStatus,
  getTasksForMemberId,
  type MemberTaskStatusFilter,
} from '../lib/workflowTaskUtils'
import { ChevronIcon } from './ChevronIcon'
import { TeamMemberStatsChart } from './TeamMemberStatsChart'

function SummaryCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-lg border border-hairline bg-surface-1 px-4 py-3">
      <p className="m-0 text-xs uppercase tracking-wide text-ink-muted">{label}</p>
      <p className="m-0 mt-1 text-2xl font-semibold text-ink">{value}</p>
    </div>
  )
}

function MemberStatBadge({
  label,
  value,
  tone,
  selected,
  onClick,
}: {
  label: string
  value: number
  tone: 'completed' | 'active' | 'overdue'
  selected: boolean
  onClick: () => void
}) {
  const toneClasses = {
    completed: {
      base: 'border-success/35 bg-success/10 text-success',
      selected: 'border-success bg-success/25 text-success ring-2 ring-success/40',
    },
    active: {
      base: 'border-primary/35 bg-primary/10 text-primary-hover',
      selected: 'border-primary bg-primary/25 text-primary-hover ring-2 ring-primary/40',
    },
    overdue: {
      base: 'border-error/35 bg-error/10 text-error',
      selected: 'border-error bg-error/25 text-error ring-2 ring-error/40',
    },
  }[tone]

  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={selected}
      className={`inline-flex cursor-pointer items-center gap-1.5 rounded-md border px-2.5 py-1 text-sm font-medium transition-colors hover:opacity-90 ${
        selected ? toneClasses.selected : toneClasses.base
      }`}
    >
      <span>{label}</span>
      <span className="font-semibold">{value}</span>
    </button>
  )
}

function MemberTaskCard({ task, projectId }: { task: TaskSummary; projectId: string }) {
  const content = (
    <>
      <h4 className="m-0 text-sm font-medium text-ink">{task.name}</h4>
      <p className="m-0 mt-1 text-xs text-ink-subtle">
        Faza: {task.phaseName?.trim() || 'Nije dodeljena'}
      </p>
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

interface TeamMemberAnalyticsProps {
  teamStats: TaskTeamMemberStats[]
  projectTasks: TaskSummary[]
  projectId: string
  projectName: string
  filters?: Partial<AnalyticsFilters>
}

export function TeamMemberAnalytics({
  teamStats,
  projectTasks,
  projectId,
  projectName,
  filters,
}: TeamMemberAnalyticsProps) {
  const memberIds = useMemo(
    () => teamStats.map((member) => String(member.memberId)),
    [teamStats],
  )
  const [collapsedMembers, setCollapsedMembers] = useState<Set<string>>(() => new Set(memberIds))
  const [memberStatusFilters, setMemberStatusFilters] = useState<
    Record<string, MemberTaskStatusFilter | null>
  >({})
  const [exportingPdf, setExportingPdf] = useState(false)
  const chartRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setCollapsedMembers(new Set(memberIds))
    setMemberStatusFilters({})
  }, [memberIds])

  function toggleMemberStatusFilter(
    memberKey: string,
    status: MemberTaskStatusFilter,
  ) {
    setMemberStatusFilters((prev) => {
      const nextFilter = prev[memberKey] === status ? null : status
      if (nextFilter) {
        setCollapsedMembers((collapsed) => {
          const next = new Set(collapsed)
          next.delete(memberKey)
          return next
        })
      }
      return { ...prev, [memberKey]: nextFilter }
    })
  }

  function toggleMember(memberId: string) {
    setCollapsedMembers((prev) => {
      const next = new Set(prev)
      if (next.has(memberId)) next.delete(memberId)
      else next.add(memberId)
      return next
    })
  }

  const totals = useMemo(
    () => ({
      assigned: teamStats.reduce((sum, member) => sum + member.totalAssignedTasks, 0),
      completed: teamStats.reduce((sum, member) => sum + member.completedTasks, 0),
      active: teamStats.reduce((sum, member) => sum + member.activeTasks, 0),
      overdue: teamStats.reduce((sum, member) => sum + member.overdueTasks, 0),
    }),
    [teamStats],
  )

  async function handleExportPdf() {
    setExportingPdf(true)
    try {
      await exportTeamMemberAnalyticsPdf({
        projectName,
        teamStats,
        filters,
        chartElement: chartRef.current,
      })
    } finally {
      setExportingPdf(false)
    }
  }

  return (
    <div className="grid gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="m-0 text-base font-semibold text-ink">Pregled statistike</h2>
        <Button
          type="button"
          variant="secondary"
          disabled={exportingPdf}
          onClick={() => void handleExportPdf()}
        >
          {exportingPdf ? 'Generisanje PDF-a...' : 'Preuzmi PDF'}
        </Button>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <SummaryCard label="Dodeljeno" value={totals.assigned} />
        <SummaryCard label="Završeno" value={totals.completed} />
        <SummaryCard label="Aktivno" value={totals.active} />
        <SummaryCard label="Prekoračeno" value={totals.overdue} />
      </div>

      <section className="grid gap-3">
        <h2 className="m-0 text-base font-semibold text-ink">Grafikoni po članovima</h2>
        <div
          ref={chartRef}
          className="rounded-lg border border-hairline bg-surface-1 p-4"
        >
          <TeamMemberStatsChart teamStats={teamStats} />
        </div>
      </section>

      <section className="grid gap-3">
        <h2 className="m-0 text-base font-semibold text-ink">Članovi tima</h2>
        {teamStats.map((member) => {
          const memberKey = String(member.memberId)
          const expanded = !collapsedMembers.has(memberKey)
          const statusFilter = memberStatusFilters[memberKey] ?? null
          const memberTasks = filterMemberTasksByStatus(
            getTasksForMemberId(projectTasks, member.memberId, filters),
            statusFilter,
          )

          return (
            <article
              key={member.memberId}
              className="overflow-hidden rounded-lg border border-hairline bg-surface-1"
            >
              <button
                type="button"
                onClick={() => toggleMember(memberKey)}
                aria-expanded={expanded}
                className="flex w-full cursor-pointer items-center justify-between gap-3 px-4 py-3 text-left transition-colors hover:bg-surface-2"
              >
                <div className="flex min-w-0 items-center gap-2.5">
                  <ChevronIcon expanded={expanded} />
                  <span className="truncate font-medium text-ink">{member.memberName}</span>
                </div>
                <span className="shrink-0 text-sm text-ink-muted">
                  {member.totalAssignedTasks} dodeljeno
                </span>
              </button>

              <div className="mt-2 flex flex-wrap gap-2 border-t border-hairline px-4 pb-3 pt-3">
                <MemberStatBadge
                  label="Završeno"
                  value={member.completedTasks}
                  tone="completed"
                  selected={statusFilter === 'completed'}
                  onClick={() => toggleMemberStatusFilter(memberKey, 'completed')}
                />
                <MemberStatBadge
                  label="Aktivno"
                  value={member.activeTasks}
                  tone="active"
                  selected={statusFilter === 'active'}
                  onClick={() => toggleMemberStatusFilter(memberKey, 'active')}
                />
                <MemberStatBadge
                  label="Prekoračeno"
                  value={member.overdueTasks}
                  tone="overdue"
                  selected={statusFilter === 'overdue'}
                  onClick={() => toggleMemberStatusFilter(memberKey, 'overdue')}
                />
              </div>

              <div
                className={`overflow-hidden border-t border-hairline transition-all duration-300 ease-out ${
                  expanded ? 'max-h-[1200px] opacity-100' : 'max-h-0 opacity-0'
                }`}
              >
                <div className="grid gap-3 px-4 py-3">
                  {memberTasks.length > 0 ? (
                    memberTasks.map((task) => (
                      <MemberTaskCard
                        key={task.id ?? `${task.name}-${task.endDate}`}
                        task={task}
                        projectId={projectId}
                      />
                    ))
                  ) : (
                    <p className="m-0 text-sm text-ink-muted">
                      {statusFilter
                        ? 'Nema zadataka za izabrani filter.'
                        : 'Nema dodeljenih zadataka u ovom projektu.'}
                    </p>
                  )}
                </div>
              </div>
            </article>
          )
        })}
      </section>
    </div>
  )
}
