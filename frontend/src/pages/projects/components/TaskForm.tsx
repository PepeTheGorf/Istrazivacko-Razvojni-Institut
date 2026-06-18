import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { fetchTechnicalResources } from '../../../api/technicalResources'
import { Button } from '../../../components/ui/Button'
import { SelectField } from '../../../components/ui/SelectField'
import { TextArea } from '../../../components/ui/TextArea'
import { TextInput } from '../../../components/ui/TextInput'
import type { TechnicalResource } from '../../../types/technicalResource'
import type { Workflow } from '../../../types/workflow'
import type { TeamMember } from '../../../types/user'
import { teamMemberLabel } from '../../../lib/teamMemberLabel'

export interface TaskFormValues {
  name: string
  description: string
  endDate: string
  workflowId: string
  assigneeId: string
  acceptanceCriteria: Array<{
    id?: number
    name: string
    description: string
  }>
  resourceAssignments: Array<{
    resourceId: number
    name: string
    quantity: number
  }>
}

interface TaskFormProps {
  mode: 'create' | 'edit'
  embedded?: boolean
  submitting: boolean
  canManage: boolean
  workflows: Workflow[]
  teamMembers?: TeamMember[]
  loadingTeamMembers?: boolean
  initialValues?: TaskFormValues
  onSubmit: (values: TaskFormValues) => Promise<void>
  onCancel?: () => void
  createSubmitLabel?: string
}

const EMPTY_VALUES: TaskFormValues = {
  name: '',
  description: '',
  endDate: '',
  workflowId: '',
  assigneeId: '',
  acceptanceCriteria: [],
  resourceAssignments: [],
}

export function TaskForm({
  mode,
  embedded = false,
  submitting,
  canManage,
  workflows,
  teamMembers = [],
  loadingTeamMembers = false,
  initialValues,
  onSubmit,
  onCancel,
  createSubmitLabel,
}: TaskFormProps) {
  const [values, setValues] = useState<TaskFormValues>(initialValues ?? EMPTY_VALUES)
  const [error, setError] = useState<string | null>(null)
  const [catalog, setCatalog] = useState<TechnicalResource[]>([])
  const [loadingCatalog, setLoadingCatalog] = useState(false)
  const [selectedResourceId, setSelectedResourceId] = useState('')
  const [resourceQuantity, setResourceQuantity] = useState('1')

  const title = useMemo(() => (mode === 'create' ? 'Novi zadatak' : 'Izmena zadatka'), [mode])

  useEffect(() => {
    setValues(initialValues ?? EMPTY_VALUES)
  }, [initialValues])

  useEffect(() => {
    if (mode !== 'create' || !canManage) return
    let active = true
    setLoadingCatalog(true)
    void fetchTechnicalResources()
      .then((items) => {
        if (active) setCatalog(items)
      })
      .finally(() => {
        if (active) setLoadingCatalog(false)
      })
    return () => {
      active = false
    }
  }, [canManage, mode])

  const assignedResourceIds = useMemo(
    () => new Set(values.resourceAssignments.map((item) => item.resourceId)),
    [values.resourceAssignments],
  )

  const availableResources = useMemo(
    () => catalog.filter((item) => item.id != null && !assignedResourceIds.has(item.id)),
    [assignedResourceIds, catalog],
  )

  const selectedResource = availableResources.find((item) => String(item.id) === selectedResourceId)
  const maxResourceQuantity = selectedResource?.quantity ?? 0
  const parsedResourceQuantity = Number(resourceQuantity)
  const resourceQuantityInvalid =
    !Number.isFinite(parsedResourceQuantity) ||
    parsedResourceQuantity <= 0 ||
    parsedResourceQuantity > maxResourceQuantity

  function addResourceAssignment() {
    if (!selectedResource?.id || resourceQuantityInvalid) {
      setError(
        maxResourceQuantity === 0
          ? 'Izabrani resurs nema dostupne količine.'
          : `Unesite količinu između 1 i ${maxResourceQuantity}.`,
      )
      return
    }
    setValues((prev) => ({
      ...prev,
      resourceAssignments: [
        ...prev.resourceAssignments,
        {
          resourceId: selectedResource.id!,
          name: selectedResource.name,
          quantity: parsedResourceQuantity,
        },
      ],
    }))
    setSelectedResourceId('')
    setResourceQuantity('1')
    setError(null)
  }

  function updateCriterion(
    index: number,
    patch: Partial<{ id?: number; name: string; description: string }>,
  ) {
    setValues((prev) => ({
      ...prev,
      acceptanceCriteria: prev.acceptanceCriteria.map((item, i) =>
        i === index ? { ...item, ...patch } : item,
      ),
    }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!canManage) {
      setError('Samo menadžer može menjati zadatke.')
      return
    }
    if (!values.name.trim()) {
      setError('Naziv zadatka je obavezan.')
      return
    }
    const criteriaWithoutNames = values.acceptanceCriteria.some((item) => !item.name.trim())
    if (criteriaWithoutNames) {
      setError('Svaki acceptance criterion mora imati naziv.')
      return
    }
    setError(null)
    await onSubmit(values)
  }

  const form = (
    <>
      {!embedded ? (
        <>
          <h2 className="m-0 text-xl font-semibold text-ink">{title}</h2>
          <p className="m-0 mt-1 text-sm text-ink-subtle">
            {mode === 'create'
              ? 'Unesite osnovne podatke, tok rada i acceptance kriterijume.'
              : 'Ažurirajte podatke i acceptance kriterijume zadatka.'}
          </p>
        </>
      ) : null}

      {error ? (
        <p
          className={
            embedded
              ? 'm-0 mb-4 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]'
              : 'm-0 mt-4 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]'
          }
        >
          {error}
        </p>
      ) : null}

      <form
        className={embedded ? 'grid gap-4' : 'mt-4 grid gap-4'}
        onSubmit={(event) => void handleSubmit(event)}
      >
        <TextInput
          label="Naziv zadatka"
          name="taskName"
          required
          disabled={!canManage || submitting}
          value={values.name}
          onChange={(event) => setValues((prev) => ({ ...prev, name: event.target.value }))}
        />
        <TextArea
          label="Opis"
          name="taskDescription"
          disabled={!canManage || submitting}
          value={values.description}
          onChange={(event) => setValues((prev) => ({ ...prev, description: event.target.value }))}
        />
        <TextInput
          label="Rok završetka"
          name="taskEndDate"
          type="datetime-local"
          className="[color-scheme:dark]"
          disabled={!canManage || submitting}
          value={values.endDate}
          onChange={(event) => setValues((prev) => ({ ...prev, endDate: event.target.value }))}
        />

        <SelectField
          label="Workflow"
          name="workflowId"
          disabled={!canManage || submitting}
          value={values.workflowId}
          onChange={(event) => setValues((prev) => ({ ...prev, workflowId: event.target.value }))}
        >
          <option value="">Bez workflow-a</option>
          {workflows.map((workflow) => (
            <option key={workflow.id ?? workflow.name} value={workflow.id}>
              {workflow.name}
            </option>
          ))}
        </SelectField>

        {mode === 'create' ? (
          <SelectField
            label="Član tima"
            name="assigneeId"
            disabled={!canManage || submitting || loadingTeamMembers}
            value={values.assigneeId}
            onChange={(event) => setValues((prev) => ({ ...prev, assigneeId: event.target.value }))}
          >
            <option value="">
              {loadingTeamMembers ? 'Učitavanje članova…' : 'Bez dodeljenog člana'}
            </option>
            {teamMembers.map((member) => (
              <option key={member.id} value={member.id}>
                {teamMemberLabel(member)}
              </option>
            ))}
          </SelectField>
        ) : null}

        <div className="rounded-md border border-hairline bg-surface-2 p-3">
          <div className="mb-2 flex items-center justify-between gap-3">
            <h3 className="m-0 text-sm font-semibold text-ink">Acceptance criteria</h3>
            <Button
              type="button"
              variant="secondary"
              icon="add"
              disabled={!canManage || submitting}
              onClick={() =>
                setValues((prev) => ({
                  ...prev,
                  acceptanceCriteria: [
                    ...prev.acceptanceCriteria,
                    { name: '', description: '' },
                  ],
                }))
              }
            >
              Dodaj
            </Button>
          </div>

          <div className="space-y-3">
            {values.acceptanceCriteria.length === 0 ? (
              <p className="m-0 text-sm text-ink-subtle">Nema dodatih kriterijuma.</p>
            ) : (
              values.acceptanceCriteria.map((criterion, index) => (
                <div
                  key={criterion.id ?? `criterion-${index}`}
                  className="rounded-md border border-hairline bg-surface-1 p-3"
                >
                  <div className="grid gap-3 md:grid-cols-[minmax(0,1fr)_auto] md:items-start">
                    <div className="grid gap-3">
                      <TextInput
                        label="Naziv kriterijuma"
                        name={`criterionName-${index}`}
                        disabled={!canManage || submitting}
                        value={criterion.name}
                        onChange={(event) => updateCriterion(index, { name: event.target.value })}
                      />
                      <TextArea
                        label="Opis kriterijuma"
                        name={`criterionDescription-${index}`}
                        disabled={!canManage || submitting}
                        value={criterion.description}
                        onChange={(event) =>
                          updateCriterion(index, { description: event.target.value })
                        }
                      />
                    </div>
                    <Button
                      type="button"
                      variant="delete"
                      icon="delete"
                      disabled={!canManage || submitting}
                      onClick={() =>
                        setValues((prev) => ({
                          ...prev,
                          acceptanceCriteria: prev.acceptanceCriteria.filter((_, i) => i !== index),
                        }))
                      }
                    >
                      Ukloni
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {mode === 'create' ? (
          <div className="rounded-md border border-hairline bg-surface-2 p-3">
            <div className="mb-2 flex items-center justify-between gap-3">
              <h3 className="m-0 text-sm font-semibold text-ink">Tehnički resursi</h3>
            </div>

            {values.resourceAssignments.length > 0 ? (
              <ul className="m-0 mb-3 flex list-none flex-col gap-2 p-0">
                {values.resourceAssignments.map((resource) => (
                  <li
                    key={resource.resourceId}
                    className="flex items-center justify-between gap-3 rounded-md border border-hairline bg-surface-1 px-3 py-2"
                  >
                    <div>
                      <p className="m-0 text-sm font-medium text-ink">{resource.name}</p>
                      <p className="m-0 mt-1 text-xs text-ink-subtle">
                        Količina: {resource.quantity}
                      </p>
                    </div>
                    <Button
                      type="button"
                      variant="delete"
                      icon="delete"
                      disabled={!canManage || submitting}
                      onClick={() =>
                        setValues((prev) => ({
                          ...prev,
                          resourceAssignments: prev.resourceAssignments.filter(
                            (item) => item.resourceId !== resource.resourceId,
                          ),
                        }))
                      }
                    >
                      Ukloni
                    </Button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="m-0 mb-3 text-sm text-ink-subtle">Nema dodatih resursa.</p>
            )}

            <div className="grid gap-3 md:grid-cols-[minmax(0,1fr)_120px_auto] md:items-end">
              <SelectField
                label="Dodaj resurs"
                name="pendingResourceId"
                disabled={loadingCatalog || submitting || availableResources.length === 0}
                value={selectedResourceId}
                onChange={(event) => {
                  setSelectedResourceId(event.target.value)
                  setResourceQuantity('1')
                }}
              >
                <option value="">
                  {loadingCatalog
                    ? 'Učitavanje resursa…'
                    : availableResources.length === 0
                      ? 'Nema dostupnih resursa'
                      : 'Izaberite resurs'}
                </option>
                {availableResources.map((resource) => (
                  <option key={resource.id} value={resource.id}>
                    {resource.name} (dostupno: {resource.quantity ?? 0})
                  </option>
                ))}
              </SelectField>

              <TextInput
                label={selectedResource ? `Količina (dostupno: ${maxResourceQuantity})` : 'Količina'}
                name="pendingResourceQuantity"
                type="number"
                min={1}
                max={maxResourceQuantity > 0 ? maxResourceQuantity : undefined}
                disabled={!selectedResourceId || submitting}
                value={resourceQuantity}
                onChange={(event) => setResourceQuantity(event.target.value)}
              />

              <Button
                type="button"
                variant="secondary"
                icon="add"
                disabled={
                  loadingCatalog ||
                  submitting ||
                  !selectedResourceId ||
                  resourceQuantityInvalid
                }
                onClick={addResourceAssignment}
              >
                Dodaj
              </Button>
            </div>
          </div>
        ) : null}

        <div className="flex flex-wrap gap-3">
          <Button type="submit" icon={mode === 'create' ? 'add' : 'edit'} disabled={submitting}>
            {submitting
              ? 'Čuvanje…'
              : mode === 'create'
                ? (createSubmitLabel ?? 'Kreiraj zadatak')
                : 'Sačuvaj izmene'}
          </Button>
          {onCancel ? (
            <Button type="button" variant="secondary" disabled={submitting} onClick={onCancel}>
              Otkaži
            </Button>
          ) : null}
        </div>
      </form>
    </>
  )

  if (embedded) {
    return form
  }

  return (
    <section className="rounded-lg border border-hairline bg-surface-1 p-4 md:p-6">
      {form}
    </section>
  )
}
