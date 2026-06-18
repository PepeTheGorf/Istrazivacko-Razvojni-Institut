import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  assignTaskToMember,
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
import { cn } from '../../lib/cn'
import { formatDate } from '../../lib/formatDate'
import type { ProjectTask } from '../../types/task'
import type { Workflow } from '../../types/workflow'
import { type TaskFormValues } from './components/TaskForm'
import { TaskFormDialog } from './components/TaskFormDialog'
import { TaskTree } from './components/TaskTree'
import { TeamMemberAssignPanel } from './components/TeamMemberAssignPanel'
import { TechnicalResourceAssignPanel } from './components/TechnicalResourceAssignPanel'
import { useTeamMembers } from './hooks/useTeamMembers'
import { teamMemberNameById } from '../../lib/teamMemberLabel'

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
    workflowId: task.workflow?.id != null ? String(task.workflow.id) : '',
    assigneeId: '',
    acceptanceCriteria:
      task.acceptanceCriteria?.map((criterion) => ({
        id: criterion.id,
        name: criterion.name,
        description: criterion.description ?? '',
      })) ?? [],
    resourceAssignments: [],
  }
}

export function TaskDetailsPage() {
  const { projectId, taskId } = useParams<{ projectId: string; taskId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const canManage = user?.role === 'MANAGER'
  const { teamMembers, loading: loadingTeamMembers } = useTeamMembers(canManage)

  const [task, setTask] = useState<ProjectTask | null>(null)
  const [workflows, setWorkflows] = useState<Workflow[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [subtaskDialogOpen, setSubtaskDialogOpen] = useState(false)
  const [assigningMember, setAssigningMember] = useState(false)
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
        projectId: Number(projectId),
        ...(values.workflowId ? { workflowId: Number(values.workflowId) } : {}),
      })

      const existingIds = new Set(
        (task?.acceptanceCriteria ?? []).map((criterion) => criterion.id).filter((id): id is number => id != null),
      )
      const submittedIds = new Set(
        values.acceptanceCriteria.map((criterion) => criterion.id).filter((id): id is number => id != null),
      )

      await Promise.all(
        values.acceptanceCriteria
          .filter((criterion) => criterion.name.trim())
          .map((criterion) => {
            const payload = {
              taskId: Number(taskId),
              name: criterion.name.trim(),
              description: criterion.description.trim(),
            }
            if (criterion.id) {
              return updateAcceptanceCriterion(String(criterion.id), payload)
            }
            return createAcceptanceCriterion(payload)
          }),
      )

      const removedIds = [...existingIds].filter((id) => !submittedIds.has(id))
      await Promise.all(removedIds.map((id) => deleteAcceptanceCriterion(String(id))))

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
        projectId: Number(projectId),
        parentTaskId: Number(taskId),
        ...(values.workflowId ? { workflowId: Number(values.workflowId) } : {}),
        ...(values.assigneeId ? { assigneeId: Number(values.assigneeId) } : {}),
      })
      await load()
      setSubtaskDialogOpen(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Kreiranje pod-zadatka nije uspelo')
    } finally {
      setSaving(false)
    }
  }

  async function handleAssignMember(memberId: number) {
    if (!taskId) return
    setAssigningMember(true)
    setError(null)
    try {
      await assignTaskToMember({
        taskId: Number(taskId),
        userId: memberId,
      })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Dodavanje člana tima nije uspelo')
      throw err
    } finally {
      setAssigningMember(false)
    }
  }

  const subTasks = task?.subTasks ?? []
  const assignedMemberIds =
    task?.assigneeIds ?? (task?.assigneeId != null ? [task.assigneeId] : [])
  const hasSubTasks = subTasks.length > 0
  const criteria = task?.acceptanceCriteria ?? []
  const completedCriteria = criteria.filter((item) => item.completed).length
  const criteriaProgress =
    criteria.length > 0 ? Math.round((completedCriteria / criteria.length) * 100) : 0

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header className="flex flex-wrap items-start justify-between gap-3">
          <div className="min-w-0">
            {projectId ? (
              <Link
                to={`/projects/${projectId}`}
                className="text-sm text-ink-subtle hover:text-primary-hover hover:underline"
              >
                Nazad na projekat
              </Link>
            ) : null}
            <h1 className="m-0 mt-2 text-3xl font-semibold tracking-tight text-ink md:text-4xl">
              {task?.name ?? 'Detalji zadatka'}
            </h1>
            {task?.description?.trim() ? (
              <p className="m-0 mt-2 text-sm text-ink-subtle">{task.description}</p>
            ) : null}
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
          <p className="m-0 text-sm text-ink-subtle">Učitavanje…</p>
        ) : (
          <>
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:gap-6">
              <div className="min-w-0 flex-1 space-y-4">
                <section className="rounded-xl border border-hairline bg-surface-1 p-4">
                  <div className="flex flex-wrap items-center justify-between gap-2 border-b border-hairline pb-3">
                    <h2 className="m-0 text-lg font-semibold text-ink">Uslovi realizacije</h2>
                    {criteria.length > 0 ? (
                      <span className="text-sm text-ink-subtle">
                        {completedCriteria}/{criteria.length} ispunjeno
                      </span>
                    ) : null}
                  </div>
                  <div
                    className={cn(
                      'mt-1',
                      criteria.length > 6 && 'scrollbar-dark max-h-[320px] overflow-y-auto pr-1',
                    )}
                  >
                    {criteria.length > 0 ? (
                      criteria.map((criterion) => (
                        <div
                          key={criterion.id ?? criterion.name}
                          className="flex items-start gap-3 border-b border-hairline py-2.5 last:border-b-0"
                        >
                          <span
                            className={cn(
                              'mt-0.5 inline-flex h-5 w-5 shrink-0 items-center justify-center rounded border',
                              criterion.completed
                                ? 'border-primary bg-primary'
                                : 'border-hairline bg-surface-2',
                            )}
                            aria-hidden
                          >
                            {criterion.completed ? (
                              <svg viewBox="0 0 16 16" className="h-3 w-3 text-on-primary">
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
                          <div className="min-w-0">
                            <p
                              className={cn(
                                'm-0 text-sm',
                                criterion.completed ? 'text-ink-subtle line-through' : 'text-ink',
                              )}
                            >
                              {criterion.name}
                            </p>
                            {criterion.description?.trim() ? (
                              <p className="m-0 mt-0.5 text-xs text-ink-subtle">
                                {criterion.description}
                              </p>
                            ) : null}
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="m-0 py-3 text-sm text-ink-subtle">
                        Nema definisanih uslova realizacije.
                      </p>
                    )}
                  </div>
                </section>

                {hasSubTasks ? (
                  <section className="rounded-xl border border-hairline bg-surface-1 p-4">
                    <h2 className="m-0 border-b border-hairline pb-3 text-lg font-semibold text-ink">
                      Podzadaci
                    </h2>
                    <div className="scrollbar-dark mt-3 max-h-[480px] overflow-y-auto pr-1">
                      {projectId ? (
                        <TaskTree tasks={subTasks} projectId={projectId} />
                      ) : (
                        <p className="m-0 text-sm text-ink-subtle">Projektni kontekst nije dostupan.</p>
                      )}
                    </div>
                  </section>
                ) : null}

                {canManage ? (
                  <section className="rounded-xl border border-hairline bg-surface-1 p-4">
                    <h2 className="m-0 text-lg font-semibold text-ink">Upravljanje zadatkom</h2>
                    <p className="m-0 mt-1 text-sm text-ink-subtle">
                      Dodela članova tima i tehničkih resursa.
                    </p>
                    <div className="mt-4 grid gap-4">
                      <TeamMemberAssignPanel
                        assignedMemberIds={assignedMemberIds}
                        teamMembers={teamMembers}
                        loadingMembers={loadingTeamMembers}
                        submitting={assigningMember}
                        onAssign={handleAssignMember}
                      />
                      {task.id ? (
                        <TechnicalResourceAssignPanel
                          taskId={task.id}
                          assignedResources={task.technicalResources ?? []}
                          submitting={saving}
                          onAssigned={load}
                        />
                      ) : null}
                    </div>
                  </section>
                ) : (task.technicalResources?.length ?? 0) > 0 ? (
                  <section className="rounded-xl border border-hairline bg-surface-1 p-4">
                    <h2 className="m-0 text-lg font-semibold text-ink">Tehnički resursi</h2>
                    <ul className="m-0 mt-3 list-none space-y-2 p-0">
                      {task.technicalResources?.map((resource) => (
                        <li
                          key={resource.resourceId}
                          className="rounded-md border border-hairline bg-surface-2 px-3 py-2"
                        >
                          <p className="m-0 text-sm font-medium text-ink">{resource.name}</p>
                          <p className="m-0 mt-1 text-xs text-ink-subtle">
                            Dodeljeno: {resource.assignedQuantity}
                          </p>
                        </li>
                      ))}
                    </ul>
                  </section>
                ) : null}
              </div>

              <aside className="w-full shrink-0 lg:sticky lg:top-20 lg:w-[280px]">
                <section className="rounded-xl border border-hairline bg-surface-1 p-4">
                  <h2 className="m-0 text-base font-semibold text-ink">Informacije</h2>
                  <dl className="m-0 mt-3 grid gap-3">
                    <div>
                      <dt className="m-0 text-xs text-ink-subtle">Faza</dt>
                      <dd className="m-0 mt-1">
                        <span className="inline-flex rounded-full border border-primary/35 bg-primary/10 px-2.5 py-0.5 text-[11px] font-medium text-primary-hover">
                          {task.phaseName ?? 'Bez faze'}
                        </span>
                      </dd>
                    </div>
                    <div>
                      <dt className="m-0 text-xs text-ink-subtle">Radni tok</dt>
                      <dd className="m-0 mt-1 text-sm text-ink">{task.workflow?.name ?? 'Nije dodeljen'}</dd>
                    </div>
                    <div>
                      <dt className="m-0 text-xs text-ink-subtle">Rok</dt>
                      <dd className="m-0 mt-1 text-sm text-error">{formatDate(task.endDate)}</dd>
                    </div>
                    {assignedMemberIds.length > 0 ? (
                      <div>
                        <dt className="m-0 text-xs text-ink-subtle">Dodeljeni članovi</dt>
                        <dd className="m-0 mt-1 text-sm text-ink">
                          {assignedMemberIds
                            .map((memberId) => teamMemberNameById(teamMembers, memberId))
                            .filter(Boolean)
                            .join(', ')}
                        </dd>
                      </div>
                    ) : null}
                  </dl>

                  <div className="mt-4 border-t border-hairline pt-4">
                    <h3 className="m-0 text-sm font-semibold text-ink">Napredak uslova</h3>
                    {criteria.length > 0 ? (
                      <>
                        <p className="m-0 mt-2 text-sm text-ink">
                          {criteriaProgress}% ({completedCriteria}/{criteria.length})
                        </p>
                        <div className="mt-2 h-2 overflow-hidden rounded-full bg-surface-3">
                          <div
                            className="h-full rounded-full bg-primary transition-all duration-300"
                            style={{ width: `${criteriaProgress}%` }}
                          />
                        </div>
                      </>
                    ) : (
                      <p className="m-0 mt-2 text-sm text-ink-subtle">Nema definisanih uslova.</p>
                    )}
                  </div>
                </section>
              </aside>
            </div>
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
            teamMembers={teamMembers}
            loadingTeamMembers={loadingTeamMembers}
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
            teamMembers={teamMembers}
            loadingTeamMembers={loadingTeamMembers}
            initialValues={{
              name: '',
              description: '',
              endDate: '',
              workflowId: task.workflow?.id != null ? String(task.workflow.id) : '',
              assigneeId: '',
              acceptanceCriteria: [],
              resourceAssignments: [],
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
