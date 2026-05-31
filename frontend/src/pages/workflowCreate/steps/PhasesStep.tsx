import { Button } from '../../../components/ui/Button'
import { TextInput } from '../../../components/ui/TextInput'
import { cn } from '../../../lib/cn'
import type { PhaseCreation } from '../../../types/workflow'
import { phaseRoleLabel } from '../../../components/workflow/workflowWizardConfig'

interface PhasesStepProps {
  phases: PhaseCreation[]
  newPhaseName: string
  onNewPhaseNameChange: (value: string) => void
  onAddPhase: () => void
  onRemovePhase: (index: number) => void
  onMovePhase: (index: number, direction: 'up' | 'down') => void
}

function ChevronIcon({ direction }: { direction: 'up' | 'down' }) {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      aria-hidden
      className={direction === 'down' ? 'rotate-180' : undefined}
    >
      <path
        d="M4 10L8 6L12 10"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function PhasesStep({
  phases,
  newPhaseName,
  onNewPhaseNameChange,
  onAddPhase,
  onRemovePhase,
  onMovePhase,
}: PhasesStepProps) {
  return (
    <div className="grid w-full gap-8 py-2">
      {phases.length > 0 ? (
        <ul className="m-0 grid list-none gap-0 p-0">
          {phases.map((phase, index) => (
            <li
              key={`${index}-${phase.name}`}
              className="grid grid-cols-[auto_1fr_auto_auto] items-center gap-4 border-b border-hairline py-4 first:pt-0 last:border-b-0"
            >
              <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full border border-hairline bg-surface-3 text-sm font-medium text-ink-muted">
                {index + 1}
              </span>

              <span className="min-w-0 truncate text-base font-semibold text-ink">
                {phase.name}
              </span>

              <span className="hidden text-sm text-ink-subtle sm:block">
                {phaseRoleLabel(index, phases.length)}
              </span>

              <div className="flex items-center gap-2 sm:gap-3">
                <div className="flex items-center gap-0.5">
                  <button
                    type="button"
                    aria-label="Pomeri fazu nagore"
                    disabled={index === 0}
                    onClick={() => onMovePhase(index, 'up')}
                    className={cn(
                      'inline-flex h-8 w-8 items-center justify-center rounded-md border border-transparent transition-colors',
                      index === 0
                        ? 'cursor-not-allowed text-ink-tertiary/40'
                        : 'text-ink-muted hover:border-hairline hover:bg-surface-2 hover:text-ink',
                    )}
                  >
                    <ChevronIcon direction="up" />
                  </button>
                  <button
                    type="button"
                    aria-label="Pomeri fazu nadole"
                    disabled={index === phases.length - 1}
                    onClick={() => onMovePhase(index, 'down')}
                    className={cn(
                      'inline-flex h-8 w-8 items-center justify-center rounded-md border border-transparent transition-colors',
                      index === phases.length - 1
                        ? 'cursor-not-allowed text-ink-tertiary/40'
                        : 'text-ink-muted hover:border-hairline hover:bg-surface-2 hover:text-ink',
                    )}
                  >
                    <ChevronIcon direction="down" />
                  </button>
                </div>

                <Button
                  type="button"
                  variant="delete"
                  icon="delete"
                  className="min-h-9 border border-error/45 px-3 py-1.5"
                  onClick={() => onRemovePhase(index)}
                >
                  Ukloni
                </Button>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <p className="m-0 text-sm text-ink-subtle">Još nema dodatih faza.</p>
      )}

      <div className="flex w-full flex-wrap items-end gap-3 border-t border-hairline pt-6">
        <div className="min-w-[200px] flex-1">
          <TextInput
            label="Naziv Faze"
            name="new-phase"
            value={newPhaseName}
            onChange={(e) => onNewPhaseNameChange(e.target.value)}
            placeholder="Upiši naziv faze..."
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault()
                onAddPhase()
              }
            }}
          />
        </div>
        <Button
          variant="secondary"
          type="button"
          icon="add"
          className="mb-0.5 shrink-0"
          onClick={onAddPhase}
        >
          Dodaj
        </Button>
      </div>
    </div>
  )
}
