import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { fetchProjectById } from '../../api/projects'
import { createAcceptanceCriterion, createTask, fetchTasksByProject } from '../../api/tasks'
import { fetchWorkflows } from '../../api/workflows'
import { useAuth } from '../../auth/AuthContext'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { PristupDialog } from '../../components/PristupDialog'
import type { Project } from '../../types/project'
import type { TaskSummary } from '../../types/task'
import type { Workflow } from '../../types/workflow'
import { CreateTaskDialog } from './components/CreateTaskDialog'
import { type TaskFormValues } from './components/TaskForm'
import { formatDate } from '../../lib/formatDate'
import { TaskTree } from './components/TaskTree'

function toIsoDateTimeOrUndefined(value: string): string | undefined {
  if (!value) return undefined
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? undefined : date.toISOString()
}

function flattenTasks(tasks: TaskSummary[]): TaskSummary[] {
  return tasks.flatMap((task) => [task, ...(task.subTasks ? flattenTasks(task.subTasks) : [])])
}

export function ProjectDetailsPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const canManage = user?.role === 'MANAGER'

  const [project, setProject] = useState<Project | null>(null)
  const [tasks, setTasks] = useState<TaskSummary[]>([])
  const [workflows, setWorkflows] = useState<Workflow[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [shareOpen, setShareOpen] = useState(false)
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
      await createTask({
        name: values.name.trim(),
        description: values.description.trim(),
        endDate: toIsoDateTimeOrUndefined(values.endDate),
        projectId,
        ...(values.workflowId ? { workflowId: values.workflowId } : null),
      })

      const reloadedTasks = await fetchTasksByProject(projectId)
      setTasks(reloadedTasks)

      const createdCandidate =
        [...flattenTasks(reloadedTasks)]
          .reverse()
          .find(
            (task) =>
              task.name.trim().toLowerCase() === values.name.trim().toLowerCase() &&
              (task.description?.trim() ?? '') === values.description.trim(),
          ) ?? null

      if (createdCandidate?.id) {
        await Promise.all(
          values.acceptanceCriteria
            .filter((item) => item.name.trim())
            .map((item) =>
              createAcceptanceCriterion({
                taskId: createdCandidate.id!,
                name: item.name.trim(),
                description: item.description.trim(),
              }),
            ),
        )
        const tasksAfterCriteria = await fetchTasksByProject(projectId)
        setTasks(tasksAfterCriteria)
      }
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
          <div>
            <p className="m-0 mb-2 text-[13px] font-medium tracking-wide text-ink-subtle uppercase">
              Projekti
            </p>
            <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
              {project?.name ?? 'Detalji projekta'}
            </h1>
            <p className="mt-2 text-sm text-ink-subtle">
              Pregled informacija projekta, dodavanje zadataka i prikaz kompletne strukture.
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button variant="secondary" onClick={() => navigate('/projects')}>
              Nazad na projekte
            </Button>
            {projectId && canManage && (
              <button
                type="button"
                onClick={() => setShareOpen(true)}
                className="flex items-center gap-1.5 rounded-md border border-hairline bg-surface-1 px-3 py-1.5 text-sm text-ink-muted transition-colors hover:bg-surface-2 hover:text-ink cursor-pointer"
              >
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden>
                  <circle cx="18" cy="5" r="3" stroke="currentColor" strokeWidth="1.75" />
                  <circle cx="6" cy="12" r="3" stroke="currentColor" strokeWidth="1.75" />
                  <circle cx="18" cy="19" r="3" stroke="currentColor" strokeWidth="1.75" />
                  <path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" />
                </svg>
                Podeli
              </button>
            )}
            {projectId && canManage ? (
              <Button icon="add" onClick={() => setCreateDialogOpen(true)}>
                Novi zadatak
              </Button>
            ) : null}
            {projectId && canManage ? (
              <Link to={`/projects/${projectId}/edit`} className="inline-flex">
                <Button variant="secondary" icon="edit">
                  Izmeni projekat
                </Button>
              </Link>
            ) : null}
          </div>
        </header>

        {error ? (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        ) : null}

        {loading ? (
          <section className="rounded-xl border border-hairline bg-surface-1 p-5">
            <p className="m-0 text-sm text-ink-subtle">Učitavanje…</p>
          </section>
        ) : (
          <>
            <section className="rounded-xl border border-hairline bg-surface-1 p-5">
              <div className="grid gap-3 md:grid-cols-3">
                <div>
                  <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">
                    Naziv
                  </p>
                  <p className="m-0 mt-1 text-sm text-ink">{project?.name ?? '-'}</p>
                </div>
                <div>
                  <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">
                    Početak
                  </p>
                  <p className="m-0 mt-1 text-sm text-ink">{formatDate(project?.startDate)}</p>
                </div>
                <div>
                  <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">
                    Kraj
                  </p>
                  <p className="m-0 mt-1 text-sm text-ink">{formatDate(project?.endDate)}</p>
                </div>
              </div>
              <div className="mt-4 border-t border-hairline pt-4">
                <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">
                  Opis
                </p>
                <p className="m-0 mt-1 text-sm text-ink-subtle">
                  {project?.description?.trim() || 'Opis nije unet.'}
                </p>
              </div>
            </section>

            <section className="rounded-xl border border-hairline bg-surface-1 p-5">
              <h2 className="m-0 text-xl font-semibold text-ink">Zadaci na projektu</h2>
              <p className="m-0 mt-1 text-sm text-ink-subtle">
                Prikaz
              </p>
              <div className="mt-4">
                {projectId ? <TaskTree tasks={tasks} projectId={projectId} /> : null}
              </div>
            </section>
          </>
        )}
      </div>
      <CreateTaskDialog
        open={createDialogOpen}
        submitting={saving}
        canManage={canManage}
        workflows={workflows}
        onClose={() => setCreateDialogOpen(false)}
        onSubmit={handleCreateTask}
      />

      {projectId && (
        <PristupDialog
          isOpen={shareOpen}
          onClose={() => setShareOpen(false)}
          resourceType="PROJEKAT"
          resourceId={projectId}
          currentUserId={user ? String(user.id) : ''}
        />
      )}
    </AppShell>
  )
}
