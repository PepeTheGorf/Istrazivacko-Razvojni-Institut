import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { fetchProjectById } from '../../api/projects'
import { createAcceptanceCriterion, createTask, fetchTasksByProject } from '../../api/tasks'
import { assignTechnicalResourceToTask } from '../../api/technicalResources'
import { fetchWorkflows } from '../../api/workflows'
import { useAuth } from '../../auth/AuthContext'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import type { Project } from '../../types/project'
import type { TaskSummary } from '../../types/task'
import type { Workflow } from '../../types/workflow'
import { CreateTaskDialog } from './components/CreateTaskDialog'
import { type TaskFormValues } from './components/TaskForm'
import { formatDate } from '../../lib/formatDate'
import { toIsoDateTimeOrUndefined } from '../../lib/datetimeInput'
import { TaskTree } from './components/TaskTree'
import { useTeamMembers } from './hooks/useTeamMembers'

export function ProjectDetailsPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const canManage = user?.role === 'MANAGER'
  const { teamMembers, loading: loadingTeamMembers } = useTeamMembers(canManage)

  const [project, setProject] = useState<Project | null>(null)
  const [tasks, setTasks] = useState<TaskSummary[]>([])
  const [workflows, setWorkflows] = useState<Workflow[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!projectId) return
    setLoading(true)
    setError(null)
    try {
      const [projectData, projectTasks, workflowData] = await Promise.all([
        fetchProjectById(projectId),
        fetchTasksByProject(projectId),
        fetchWorkflows(),
      ])
      setProject(projectData)
      setTasks(projectTasks)
      setWorkflows(workflowData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje detalja projekta nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    void load()
  }, [load])

  async function handleCreateTask(values: TaskFormValues) {
    if (!projectId) return
    setSaving(true)
    setError(null)
    try {
      const createdTask = await createTask({
        name: values.name.trim(),
        description: values.description.trim(),
        startDate: toIsoDateTimeOrUndefined(values.startDate),
        endDate: toIsoDateTimeOrUndefined(values.endDate),
        projectId: Number(projectId),
        ...(values.workflowId ? { workflowId: Number(values.workflowId) } : {}),
        ...(values.assigneeId ? { assigneeId: Number(values.assigneeId) } : {}),
      })

      if (createdTask.id) {
        await Promise.all([
          ...values.acceptanceCriteria
            .filter((item) => item.name.trim())
            .map((item) =>
              createAcceptanceCriterion({
                taskId: createdTask.id!,
                name: item.name.trim(),
                description: item.description.trim(),
              }),
            ),
          ...values.resourceAssignments.map((item) =>
            assignTechnicalResourceToTask(item.resourceId, createdTask.id!, item.quantity),
          ),
        ])
      }

      const reloadedTasks = await fetchTasksByProject(projectId)
      setTasks(reloadedTasks)
      setCreateDialogOpen(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Kreiranje zadatka nije uspelo')
    } finally {
      setSaving(false)
    }
  }

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header className="flex flex-wrap items-start justify-between gap-3">
          <div className="min-w-0">
            <Link
              to="/projects"
              className="text-sm text-ink-subtle hover:text-primary-hover hover:underline"
            >
              Nazad na projekte
            </Link>
            <h1 className="m-0 mt-2 text-3xl font-semibold tracking-tight text-ink md:text-4xl">
              {project?.name ?? 'Detalji projekta'}
            </h1>
            {project?.description?.trim() ? (
              <p className="m-0 mt-2 text-sm text-ink-subtle">{project.description}</p>
            ) : null}
          </div>
          {canManage ? (
            <div className="flex flex-wrap gap-2">
              {projectId ? (
                <Button icon="add" onClick={() => setCreateDialogOpen(true)}>
                  Novi zadatak
                </Button>
              ) : null}
              {projectId ? (
                <Link to={`/projects/${projectId}/edit`} className="inline-flex">
                  <Button variant="secondary" icon="edit">
                    Izmeni projekat
                  </Button>
                </Link>
              ) : null}
            </div>
          ) : (
            <Button variant="secondary" onClick={() => navigate('/projects')}>
              Nazad
            </Button>
          )}
        </header>

        {error ? (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        ) : null}

        {loading ? (
          <p className="m-0 text-sm text-ink-subtle">Učitavanje...</p>
        ) : (
          <section className="rounded-xl border border-hairline bg-surface-1 p-4">
            <div className="flex flex-wrap items-end justify-between gap-3 border-b border-hairline pb-4">
              <div>
                <h2 className="m-0 text-lg font-semibold text-ink">Zadaci</h2>
                <p className="m-0 mt-1 text-sm text-ink-subtle">
                  {tasks.length} {tasks.length === 1 ? 'zadatak' : 'zadataka'}
                </p>
              </div>
              <dl className="m-0 flex flex-wrap gap-x-6 gap-y-1 text-sm">
                <div>
                  <dt className="inline text-ink-subtle">Početak: </dt>
                  <dd className="inline text-ink">{formatDate(project?.startDate)}</dd>
                </div>
                <div>
                  <dt className="inline text-ink-subtle">Kraj: </dt>
                  <dd className="inline text-ink">{formatDate(project?.endDate)}</dd>
                </div>
              </dl>
            </div>
            <div className="scrollbar-dark mt-4 max-h-[calc(100vh-280px)] min-h-[200px] overflow-y-auto pr-1">
              {projectId ? <TaskTree tasks={tasks} projectId={projectId} /> : null}
            </div>
          </section>
        )}
      </div>
      <CreateTaskDialog
        open={createDialogOpen}
        submitting={saving}
        canManage={canManage}
        workflows={workflows}
        teamMembers={teamMembers}
        loadingTeamMembers={loadingTeamMembers}
        dateConstraints={{
          projectStartDate: project?.startDate,
          projectEndDate: project?.endDate,
        }}
        onClose={() => setCreateDialogOpen(false)}
        onSubmit={handleCreateTask}
      />
    </AppShell>
  )
}
