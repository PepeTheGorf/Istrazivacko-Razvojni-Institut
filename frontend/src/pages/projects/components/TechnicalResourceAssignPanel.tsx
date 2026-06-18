import { useEffect, useMemo, useState } from 'react'
import {
  assignTechnicalResourceToTask,
  fetchTechnicalResources,
} from '../../../api/technicalResources'
import { Button } from '../../../components/ui/Button'
import { SelectField } from '../../../components/ui/SelectField'
import { TextInput } from '../../../components/ui/TextInput'
import type { TaskResourceAssignment } from '../../../types/task'
import type { TechnicalResource } from '../../../types/technicalResource'

interface TechnicalResourceAssignPanelProps {
  taskId: number
  assignedResources: TaskResourceAssignment[]
  submitting?: boolean
  embedded?: boolean
  onAssigned: () => Promise<void>
}

export function TechnicalResourceAssignPanel({
  taskId,
  assignedResources,
  submitting = false,
  embedded = false,
  onAssigned,
}: TechnicalResourceAssignPanelProps) {
  const [catalog, setCatalog] = useState<TechnicalResource[]>([])
  const [loadingCatalog, setLoadingCatalog] = useState(true)
  const [selectedResourceId, setSelectedResourceId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [assigning, setAssigning] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    setLoadingCatalog(true)
    void fetchTechnicalResources()
      .then((items) => {
        if (active) setCatalog(items)
      })
      .catch(() => {
        if (active) setCatalog([])
      })
      .finally(() => {
        if (active) setLoadingCatalog(false)
      })
    return () => {
      active = false
    }
  }, [])

  const assignedIds = useMemo(
    () => new Set(assignedResources.map((item) => item.resourceId)),
    [assignedResources],
  )

  const availableResources = useMemo(
    () => catalog.filter((item) => item.id != null && !assignedIds.has(item.id)),
    [assignedIds, catalog],
  )

  const selectedResource = availableResources.find(
    (item) => String(item.id) === selectedResourceId,
  )
  const maxQuantity = selectedResource?.quantity ?? 0
  const parsedQuantity = Number(quantity)
  const quantityInvalid =
    !Number.isFinite(parsedQuantity) ||
    parsedQuantity <= 0 ||
    parsedQuantity > maxQuantity

  async function handleAssign() {
    if (!selectedResourceId || quantityInvalid) {
      setError(
        maxQuantity === 0
          ? 'Izabrani resurs nema dostupne količine.'
          : `Unesite količinu između 1 i ${maxQuantity}.`,
      )
      return
    }
    setAssigning(true)
    setError(null)
    try {
      await assignTechnicalResourceToTask(Number(selectedResourceId), taskId, parsedQuantity)
      setSelectedResourceId('')
      setQuantity('1')
      await onAssigned()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Dodela resursa nije uspela.')
    } finally {
      setAssigning(false)
    }
  }

  return (
    <div className={embedded ? '' : 'rounded-md border border-hairline bg-surface-2 p-3'}>
      {!embedded ? <h3 className="m-0 text-sm font-semibold text-ink">Tehnički resursi</h3> : null}

      {assignedResources.length > 0 ? (
        <ul className="m-0 mt-3 flex list-none flex-col gap-2 p-0">
          {assignedResources.map((resource) => (
            <li
              key={resource.resourceId}
              className="rounded-md border border-hairline bg-surface-1 px-3 py-2"
            >
              <p className="m-0 text-sm font-medium text-ink">{resource.name}</p>
              <p className="m-0 mt-1 text-xs text-ink-subtle">
                Dodeljeno: {resource.assignedQuantity} · Dostupno na skladištu:{' '}
                {resource.availableQuantity}
              </p>
            </li>
          ))}
        </ul>
      ) : (
        <p className="m-0 mt-3 text-sm text-ink-subtle">Nema dodeljenih resursa.</p>
      )}

      {error ? (
        <p className="m-0 mt-3 rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
          {error}
        </p>
      ) : null}

      <div className="mt-3 grid gap-3">
        <SelectField
          label="Resurs"
          name="technicalResourceId"
          disabled={loadingCatalog || assigning || submitting || availableResources.length === 0}
          value={selectedResourceId}
          onChange={(event) => {
            setSelectedResourceId(event.target.value)
            setQuantity('1')
            setError(null)
          }}
        >
          <option value="">
            {loadingCatalog
              ? 'Učitavanje…'
              : availableResources.length === 0
                ? 'Nema resursa'
                : 'Izaberite resurs'}
          </option>
          {availableResources.map((resource) => (
            <option key={resource.id} value={resource.id}>
              {resource.name} (dostupno: {resource.quantity ?? 0})
            </option>
          ))}
        </SelectField>

        <div className="grid gap-3 sm:grid-cols-[minmax(0,140px)_auto] sm:items-end">
          <TextInput
            label={selectedResource ? `Količina (max ${maxQuantity})` : 'Količina'}
            name="resourceQuantity"
            type="number"
            min={1}
            max={maxQuantity > 0 ? maxQuantity : undefined}
            disabled={!selectedResourceId || assigning || submitting}
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
          />

          <Button
            type="button"
            icon="add"
            disabled={
              loadingCatalog ||
              assigning ||
              submitting ||
              !selectedResourceId ||
              quantityInvalid
            }
            onClick={() => void handleAssign()}
            className="sm:mb-0"
          >
            Dodaj resurs
          </Button>
        </div>
      </div>
    </div>
  )
}
