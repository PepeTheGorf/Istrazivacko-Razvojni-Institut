import { PhasePipelinePreview } from '../../../components/workflow/PhasePipelinePreview'
import type { PhaseCreation } from '../../../types/workflow'

interface ReviewStepProps {
  name: string
  description: string
  phases: PhaseCreation[]
}

export function ReviewStep({ name, description, phases }: ReviewStepProps) {
  const sorted = phases
    .map((p, idx) => ({ ...p, order: idx + 1 }))
    .filter((p) => p.name.trim())

  return (
    <div className="grid w-full gap-8 py-2">
      <div>
        <h2 className="m-0 text-xl font-semibold tracking-tight text-ink md:text-2xl">{name || '—'}</h2>
        <p className="m-0 mt-2 text-sm text-ink-subtle md:text-base">
          {description.trim() || 'Radni tok bez opisa.'}
        </p>
      </div>

      <div className="w-full">
        <h3 className="m-0 mb-4 text-xs font-semibold tracking-wider text-ink-subtle uppercase">
          Vizuelni Tok Rada
        </h3>
        <div className="w-full rounded-lg border border-hairline bg-surface-2 p-6">
          <div className="flex flex-wrap items-center gap-3">
            {sorted.map((phase, index) => (
              <div key={phase.order} className="flex items-center gap-3">
                <div className="flex flex-col items-center gap-1">
                  <span className="flex h-7 w-7 items-center justify-center rounded-full border border-hairline bg-surface-3 text-xs font-medium text-ink">
                    {index + 1}
                  </span>
                  <span className="rounded-md border border-hairline bg-surface-3 px-3 py-2 text-sm text-ink">
                    {phase.name}
                  </span>
                </div>
                {index < sorted.length - 1 && (
                  <span className="text-lg text-ink-tertiary" aria-hidden>
                    →
                  </span>
                )}
              </div>
            ))}
          </div>
          <div className="mt-6 border-t border-hairline pt-4">
            <PhasePipelinePreview phases={sorted} compact />
          </div>
        </div>
      </div>
    </div>
  )
}
