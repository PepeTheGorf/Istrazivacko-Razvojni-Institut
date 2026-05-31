import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Button } from '../../../components/ui/Button'
import { SelectField } from '../../../components/ui/SelectField'
import { TextArea } from '../../../components/ui/TextArea'
import { TextInput } from '../../../components/ui/TextInput'
import type { Workflow } from '../../../types/workflow'

export interface TaskFormValues {
  name: string
  description: string
  endDate: string
  workflowId: string
  acceptanceCriteria: Array<{
    id?: string
    name: string
    description: string
  }>
}

interface TaskFormProps {
  mode: 'create' | 'edit'
  embedded?: boolean
  submitting: boolean
  canManage: boolean
  workflows: Workflow[]
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
  acceptanceCriteria: [],
}

export function TaskForm({
  mode,
  embedded = false,
  submitting,
  canManage,
  workflows,
  initialValues,
  onSubmit,
  onCancel,
  createSubmitLabel,
}: TaskFormProps) {
  const [values, setValues] = useState<TaskFormValues>(initialValues ?? EMPTY_VALUES)
  const [error, setError] = useState<string | null>(null)

  const title = useMemo(() => (mode === 'create' ? 'Novi zadatak' : 'Izmena zadatka'), [mode])

  useEffect(() => {
    setValues(initialValues ?? EMPTY_VALUES)
  }, [initialValues])

  function updateCriterion(
    index: number,
    patch: Partial<{ id?: string; name: string; description: string }>,
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
