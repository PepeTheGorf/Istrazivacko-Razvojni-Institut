import { Button } from '../ui/Button'
import type { TransitionConditionType } from '../../types/workflow'

interface TransitionConditionTypesModalProps {
  open: boolean
  types: TransitionConditionType[]
  loading?: boolean
  onClose: () => void
}

export function TransitionConditionTypesModal({
  open,
  types,
  loading,
  onClose,
}: TransitionConditionTypesModalProps) {
  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="transition-types-modal-title"
      onClick={onClose}
    >
      <div
        className="scrollbar-dark flex max-h-[85vh] w-full max-w-2xl flex-col rounded-lg border border-hairline bg-surface-1 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-3 border-b border-hairline px-5 py-4">
          <div>
            <h2 id="transition-types-modal-title" className="m-0 text-lg font-semibold text-ink">
              Tipovi uslova prelaza
            </h2>
            <p className="m-0 mt-1 text-sm text-ink-subtle">
              Pregled svih uslova koje sistemski administrator može dodeliti prelazu.
            </p>
          </div>
          <Button variant="secondary" onClick={onClose} className="shrink-0">
            Zatvori
          </Button>
        </div>

        <div className="scrollbar-dark overflow-y-auto px-5 py-4">
          {loading ? (
            <p className="m-0 text-sm text-ink-subtle">Učitavanje...</p>
          ) : types.length === 0 ? (
            <p className="m-0 text-sm text-ink-subtle">Nema definisanih tipova uslova.</p>
          ) : (
            <ul className="m-0 grid list-none gap-3 p-0">
              {types.map((type) => (
                <li
                  key={type.id}
                  className="rounded-lg border border-hairline bg-surface-2 px-4 py-3.5"
                >
                  <p className="m-0 text-sm font-medium text-ink">{type.name}</p>
                  <p className="m-0 mt-2 text-sm leading-relaxed text-ink-muted">{type.description}</p>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  )
}
