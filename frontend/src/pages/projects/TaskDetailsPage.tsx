import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  createAcceptanceCriterion,
  createTask,
  deleteAcceptanceCriterion,
  deleteTask,
  fetchTaskById,
  updateAcceptanceCriterion,
  updateTask,
} from '../../api/tasks'
import { fetchWorkflows } from '../../api/workflows'
import { useAuth } from '../../auth/AuthContext'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { formatDate } from '../../lib/formatDate'
import type { ProjectTask } from '../../types/task'
import type { Workflow } from '../../types/workflow'
import { type TaskFormValues } from './components/TaskForm'
import { TaskFormDialog } from './components/TaskFormDialog'
import { TaskTree } from './components/TaskTree'

function toIsoDateTimeOrUndefined(value: string): string | undefined {
  if (!value) return undefined
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? undefined : date.toISOString()
}

function toFormValues(task: ProjectTask): TaskFormValues {
  return {
    name: task.name ?? '',
    description: task.description ?? '',
    endDate: '',
    workflowId: task.workflow?.id ?? '',
    acceptanceCriteria:
      task.acceptanceCriteria?.map((criterion) => ({
        id: criterion.id,
        name: criterion.name,
        description: criterion.description ?? '',
      })) ?? [],
  }
}

export function TaskDetailsPage() {
  const { projectId, taskId } = useParams<{ projectId: string; taskId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const canManage = user?.role === 'MANAGER'

  const [task, setTask] = useState<ProjectTask | null>(null)
  const [workflows, setWorkflows] = useState<Workflow[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [subtaskDialogOpen, setSubtaskDialogOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!taskId) return
    setLoading(true)
    setError(null)
    try {
      const [taskData, workflowData] = await Promise.all([fetchTaskById(taskId), fetchWorkflows()])
      setTask(taskData)
      setWorkflows(workflowData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje zadatka nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [taskId])

  useEffect(() => {
    void load()
  }, [load])

  async function handleUpdate(values: TaskFormValues) {
    if (!taskId || !projectId) return
    setSaving(true)
    setError(null)
    try {
      await updateTask(taskId, {
        name: values.name.trim(),
        description: values.description.trim(),
        endDate: toIsoDateTimeOrUndefined(values.endDate),
        projectId,
        ...(values.workflowId ? { workflowId: values.workflowId } : null),
      })

      const existingIds = new Set(
        (task?.acceptanceCriteria ?? []).map((criterion) => criterion.id).filter(Boolean) as string[],
      )
      const submittedIds = new Set(
        values.acceptanceCriteria.map((criterion) => criterion.id).filter(Boolean) as string[],
      )

      await Promise.all(
        values.acceptanceCriteria
          .filter((criterion) => criterion.name.trim())
          .map((criterion) => {
            const payload = {
              taskId,
              name: criterion.name.trim(),
              description: criterion.description.trim(),
            }
            if (criterion.id) {
              return updateAcceptanceCriterion(criterion.id, payload)
            }
            return createAcceptanceCriterion(payload)
          }),
      )

      const removedIds = [...existingIds].filter((id) => !submittedIds.has(id))
      await Promise.all(removedIds.map((id) => deleteAcceptanceCriterion(id)))

      await load()
      setEditDialogOpen(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Izmena zadatka nije uspela')
    } finally {
      setSaving(false)
    }
  }

  async function handleDeleteTask() {
    if (!taskId || !projectId || !canManage) return
    setDeleting(true)
    setError(null)
    try {
      await deleteTask(taskId)
      navigate(`/projects/${projectId}`, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Brisanje zadatka nije uspelo')
    } finally {
      setDeleting(false)
    }
  }

  async function handleCreateSubTask(values: TaskFormValues) {
    if (!projectId || !taskId) return
    setSaving(true)
    setError(null)
    try {
      await createTask({
        name: values.name.trim(),
        description: values.description.trim(),
        endDate: toIsoDateTimeOrUndefined(values.endDate),
        projectId,
        parentTaskId: taskId,
        ...(values.workflowId ? { workflowId: values.workflowId } : null),
      })
      await load()
      setSubtaskDialogOpen(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Kreiranje pod-zadatka nije uspelo')
    } finally {
      setSaving(false)
    }
  }

  const subTasks = task?.subTasks ?? []
  const hasSubTasks = subTasks.length > 0

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header className="flex flex-wrap items-start justify-between gap-3">
          <div className="min-w-0">
            {projectId ? (
              <Link
                to={`/projects/${projectId}`}
                className="mb-3 inline-flex text-sm text-ink-subtle transition-colors hover:text-ink"
              >
                ← Nazad na projekat
              </Link>
            ) : null}
            <p className="m-0 mb-2 text-[13px] font-medium tracking-wide text-ink-subtle uppercase">
              Zadaci
            </p>
            <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
              {task?.name ?? 'Detalji zadatka'}
            </h1>
            <p className="mt-2 text-sm text-ink-subtle">
              Pregled osnovnih informacija o zadatku i njegovih pod-zadataka.
            </p>
          </div>
          {canManage && task ? (
            <div className="flex flex-wrap gap-2">
              <Button icon="edit" onClick={() => setEditDialogOpen(true)}>
                Izmeni
              </Button>
              <Button icon="add" onClick={() => setSubtaskDialogOpen(true)}>
                Dodaj pod-zadatak
              </Button>
              <Button
                variant="delete"
                icon="delete"
                className="min-w-24 border border-error/45"
                disabled={deleting}
                onClick={() => void handleDeleteTask()}
              >
                {deleting ? 'Brisanje…' : 'Obriši'}
              </Button>
            </div>
          ) : null}
        </header>

        {error ? (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        ) : null}

        {loading || !task ? (
          <section className="rounded-xl border border-hairline bg-surface-1 p-5">
            <p className="m-0 text-sm text-ink-subtle">Učitavanje…</p>
          </section>
        ) : (
          <>
            <section className="rounded-xl border border-hairline bg-surface-1 p-5">
              <div className="grid gap-3 md:grid-cols-3">
                <div>
                  <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">Faza</p>
                  <p className="m-0 mt-1 text-sm text-ink">{task.phaseName ?? 'Bez faze'}</p>
                </div>
                <div>
                  <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">
                    Workflow
                  </p>
                  <p className="m-0 mt-1 text-sm text-ink">{task.workflow?.name ?? 'Nije dodeljen'}</p>
                </div>
                <div>
                  <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">
                    Rok završetka
                  </p>
                  <p className="m-0 mt-1 text-sm text-ink">{formatDate(task.endDate)}</p>
                </div>
              </div>
              <div className="mt-4 border-t border-hairline pt-4">
                <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">Opis</p>
                <p className="m-0 mt-1 text-sm text-ink-subtle">
                  {task.description?.trim() || 'Opis nije unet.'}
                </p>
              </div>
              {(task.acceptanceCriteria?.length ?? 0) > 0 ? (
                <div className="mt-4 border-t border-hairline pt-4">
                  <p className="m-0 text-xs font-medium tracking-wide text-ink-subtle uppercase">
                    Acceptance criteria
                  </p>
                  <ul className="m-0 mt-2 list-none space-y-2 p-0">
                    {task.acceptanceCriteria?.map((criterion) => (
                      <li
                        key={criterion.id ?? criterion.name}
                        className="rounded-md border border-hairline bg-surface-2 px-3 py-2"
                      >
                        <p className="m-0 text-sm font-medium text-ink">{criterion.name}</p>
                        {criterion.description?.trim() ? (
                          <p className="m-0 mt-1 text-sm text-ink-subtle">{criterion.description}</p>
                        ) : null}
                      </li>
                    ))}
                  </ul>
                </div>
              ) : null}
            </section>

            {hasSubTasks ? (
              <section className="rounded-xl border border-hairline bg-surface-1 p-5">
                <h2 className="m-0 text-xl font-semibold text-ink">Pod-zadaci</h2>
                <p className="m-0 mt-1 text-sm text-ink-subtle">
                  Hijerarhijski prikaz pod-zadataka ovog zadatka.
                </p>
                <div className="mt-4">
                  {projectId ? (
                    <TaskTree tasks={subTasks} projectId={projectId} />
                  ) : (
                    <p className="m-0 text-sm text-ink-subtle">Projektni kontekst nije dostupan.</p>
                  )}
                </div>
              </section>
            ) : null}
          </>
        )}
      </div>

      {task ? (
        <>
          <TaskFormDialog
            open={editDialogOpen}
            title="Izmena zadatka"
            subtitle={task.name}
            mode="edit"
            submitting={saving}
            canManage={canManage}
            workflows={workflows}
            initialValues={toFormValues(task)}
            onClose={() => setEditDialogOpen(false)}
            onSubmit={handleUpdate}
          />
          <TaskFormDialog
            open={subtaskDialogOpen}
            title="Novi pod-zadatak"
            subtitle={`Pod-zadatak za: ${task.name}`}
            mode="create"
            submitting={saving}
            canManage={canManage}
            workflows={workflows}
            initialValues={{
              name: '',
              description: '',
              endDate: '',
              workflowId: task.workflow?.id ?? '',
              acceptanceCriteria: [],
            }}
            createSubmitLabel="Kreiraj pod-zadatak"
            onClose={() => setSubtaskDialogOpen(false)}
            onSubmit={handleCreateSubTask}
          />
        </>
      ) : null}
    </AppShell>
  )
}
