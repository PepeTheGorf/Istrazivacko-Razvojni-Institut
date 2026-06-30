import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { SelectField } from '../../components/ui/SelectField'
import { TextInput } from '../../components/ui/TextInput'
import { TeamMemberAnalytics } from './components/TeamMemberAnalytics'
import { WorkflowPhaseAnalytics } from './components/WorkflowPhaseAnalytics'
import { useManagerAnalyticsPage } from './hooks/useManagerAnalyticsPage'

export function ManagerAnalyticsPage() {
  const {
    projects,
    selectedProjectId,
    setSelectedProjectId,
    selectedProject,
    statistic,
    setStatistic,
    filters,
    updateFilter,
    clearFilters,
    hasActiveFilters,
    memberOptions,
    taskOptions,
    workflow,
    projectTasks,
    teamStats,
    loadingProjects,
    loadingOptions,
    loadingAnalytics,
    error,
    reloadAnalytics,
  } = useManagerAnalyticsPage()

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <h1 className="m-0 text-2xl font-semibold text-ink">Analitika projekata</h1>
            <p className="m-0 mt-1 text-sm text-ink-muted">
              Pregled workflow statistike i opterećenja članova tima po projektu.
            </p>
          </div>
          <Button type="button" variant="secondary" onClick={() => void reloadAnalytics()}>
            Osveži
          </Button>
        </div>

        <div className="grid gap-4 rounded-lg border border-hairline bg-surface-1 p-4">
          <div className="grid gap-4 md:grid-cols-2">
            <SelectField
              label="Projekat"
              value={selectedProjectId}
              onChange={(event) => setSelectedProjectId(event.target.value)}
              disabled={loadingProjects || projects.length === 0}
            >
              {projects.length === 0 ? (
                <option value="">Nema projekata</option>
              ) : (
                projects.map((project) => (
                  <option key={project.id} value={project.id}>
                    {project.name}
                  </option>
                ))
              )}
            </SelectField>

            <SelectField
              label="Prikaz"
              value={statistic}
              onChange={(event) => setStatistic(event.target.value as 'workflow' | 'team')}
            >
              <option value="workflow">Statistika po fazama radnog toka</option>
              <option value="team">Statistika o članovima tima</option>
            </SelectField>
          </div>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <TextInput
              label="Period od"
              type="datetime-local"
              value={filters.from}
              onChange={(event) => updateFilter('from', event.target.value)}
            />
            <TextInput
              label="Period do"
              type="datetime-local"
              value={filters.to}
              onChange={(event) => updateFilter('to', event.target.value)}
            />
            <SelectField
              label="Član tima"
              value={filters.memberId}
              onChange={(event) => updateFilter('memberId', event.target.value)}
              disabled={loadingOptions}
            >
              <option value="">Svi članovi</option>
              {memberOptions.map((member) => (
                <option key={member.memberId} value={member.memberId}>
                  {member.memberName}
                </option>
              ))}
            </SelectField>
            <SelectField
              label="Zadatak"
              value={filters.taskId}
              onChange={(event) => updateFilter('taskId', event.target.value)}
              disabled={loadingOptions}
            >
              <option value="">Svi zadaci</option>
              {taskOptions.map((task) => (
                <option key={task.id} value={task.id}>
                  {task.name}
                </option>
              ))}
            </SelectField>
          </div>

          {hasActiveFilters && (
            <div className="flex justify-end">
              <Button type="button" variant="secondary" onClick={clearFilters}>
                Poništi filtere
              </Button>
            </div>
          )}
        </div>

        {error && (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        )}

        {(loadingAnalytics || loadingOptions) && (
          <p className="m-0 text-sm text-ink-muted">Učitavanje podataka...</p>
        )}

        {statistic === 'workflow' && workflow && (
          <WorkflowPhaseAnalytics
            workflow={workflow}
            projectTasks={projectTasks}
            projectId={selectedProjectId}
            filters={filters}
          />
        )}

        {statistic === 'team' && teamStats.length > 0 && (
          <TeamMemberAnalytics
            teamStats={teamStats}
            projectTasks={projectTasks}
            projectId={selectedProjectId}
            projectName={selectedProject?.name ?? workflow?.projectName ?? ''}
            filters={filters}
          />
        )}

        {statistic === 'team' && !loadingAnalytics && teamStats.length === 0 && selectedProjectId && (
          <p className="m-0 rounded-md border border-hairline bg-surface-1 px-3 py-3 text-sm text-ink-muted">
            Nema dodeljenih članova tima za projekat {selectedProject?.name ?? ''}.
          </p>
        )}
      </div>
    </AppShell>
  )
}
