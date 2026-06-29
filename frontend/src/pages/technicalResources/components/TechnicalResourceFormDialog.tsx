import { useEffect, useState, type FormEvent } from 'react'
import { Button } from '../../../components/ui/Button'
import { TextArea } from '../../../components/ui/TextArea'
import { TextInput } from '../../../components/ui/TextInput'
import type { TechnicalResource, TechnicalResourcePayload } from '../../../types/technicalResource'

interface TechnicalResourceFormDialogProps {
  open: boolean
  resource: TechnicalResource | null
  saving: boolean
  onClose: () => void
  onSave: (payload: TechnicalResourcePayload) => Promise<void>
}

export function TechnicalResourceFormDialog({
  open,
  resource,
  saving,
  onClose,
  onSave,
}: TechnicalResourceFormDialogProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setName(resource?.name ?? '')
    setDescription(resource?.description ?? '')
    setQuantity(String(resource?.quantity ?? 1))
    setError(null)
  }, [open, resource])

  if (!open) return null

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const parsedQuantity = Number(quantity)
    if (!name.trim()) {
      setError('Naziv resursa je obavezan.')
      return
    }
    if (!Number.isFinite(parsedQuantity) || parsedQuantity < 0) {
      setError('Količina mora biti nula ili veća.')
      return
    }
    setError(null)
    await onSave({
      name: name.trim(),
      description: description.trim(),
      quantity: parsedQuantity,
    })
  }

  return (
    <div className="fixed inset-0 z-40 grid place-items-center bg-black/60 p-4">
      <div className="w-full max-w-lg rounded-lg border border-hairline bg-surface-1 p-6">
        <h2 className="m-0 text-lg font-semibold text-ink">
          {resource ? 'Izmena resursa' : 'Novi tehnički resurs'}
        </h2>
        <p className="m-0 mt-1 text-sm text-ink-subtle">
          Definišite naziv, opis i ukupnu dostupnu količinu resursa.
        </p>

        {error ? (
          <p className="m-0 mt-3 rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
            {error}
          </p>
        ) : null}

        <form className="mt-4 grid gap-4" onSubmit={(event) => void handleSubmit(event)}>
          <TextInput
            label="Naziv"
            name="resourceName"
            required
            disabled={saving}
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
          <TextArea
            label="Opis"
            name="resourceDescription"
            disabled={saving}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
          <TextInput
            label="Količina na skladištu"
            name="resourceQuantity"
            type="number"
            min={0}
            required
            disabled={saving}
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
          />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" disabled={saving} onClick={onClose}>
              Otkaži
            </Button>
            <Button type="submit" icon={resource ? 'edit' : 'add'} disabled={saving}>
              {saving ? 'Čuvanje...' : resource ? 'Sačuvaj' : 'Kreiraj'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
