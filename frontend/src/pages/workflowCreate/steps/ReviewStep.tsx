import { useEffect, useState } from 'react'
import { fetchTransitionConditionTypes } from '../../../api/workflows'
import { routeKey } from '../../../lib/transitionRoutes'
import type { PhaseCreation, TransitionConditionCreation } from '../../../types/workflow'

interface ReviewStepProps {
  name: string
  description: string
  phases: PhaseCreation[]
  transitionConditions: TransitionConditionCreation[]
}

export function ReviewStep({ name, description, phases, transitionConditions }: ReviewStepProps) {
  const [typeNames, setTypeNames] = useState<Map<number, string>>(new Map())

  useEffect(() => {
    void fetchTransitionConditionTypes().then((types) => {
      setTypeNames(new Map(types.map((t) => [t.id, t.name])))
    })
  }, [])

  const sorted = phases
    .map((p, idx) => ({ ...p, order: idx + 1 }))
    .filter((p) => p.name.trim())

  return (
    <div className="grid w-full gap-8 py-2">
      <div>
        <h2 className="m-0 text-xl font-semibold tracking-tight text-ink md:text-2xl">
          {name || 'Bez naziva'}
        </h2>
        <p className="m-0 mt-2 text-sm text-ink-subtle md:text-base">
          {description.trim() || 'Radni tok bez opisa.'}
        </p>
      </div>

      <div className="w-full">
        <h3 className="m-0 mb-4 text-xs font-semibold tracking-wider text-ink-subtle uppercase">
          Vizuelni Tok Rada
        </h3>
        <div className="w-full rounded-lg border border-hairline bg-surface-2 p-6 md:p-8">
          <div className="flex flex-wrap items-center justify-center gap-3 md:gap-4">
            {sorted.map((phase, index) => (
              <div key={phase.order} className="flex items-center gap-3 md:gap-4">
                <div className="flex flex-col items-center gap-1.5">
                  <span className="flex h-7 w-7 items-center justify-center rounded-full border border-hairline bg-surface-3 text-xs font-medium text-ink">
                    {index + 1}
                  </span>
                  <span className="rounded-md border border-hairline bg-surface-3 px-3 py-2 text-sm font-medium text-ink">
                    {phase.name}
                  </span>
                </div>
                {index < sorted.length - 1 && (
                  <span className="text-lg text-ink-tertiary select-none" aria-hidden>
                    -
                  </span>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="w-full">
        <h3 className="m-0 mb-4 text-xs font-semibold tracking-wider text-ink-subtle uppercase">
          Uslovi prelaza
        </h3>
        {transitionConditions.length === 0 ? (
          <p className="m-0 rounded-lg border border-dashed border-hairline bg-surface-2 px-4 py-6 text-center text-sm text-ink-subtle">
            Nema definisanih uslova prelaza.
          </p>
        ) : (
          <ul className="m-0 grid list-none gap-3 p-0">
            {transitionConditions.map((route) => (
              <li
                key={routeKey(route.from, route.to)}
                className="rounded-lg border border-hairline bg-surface-2 px-4 py-4 md:px-5"
              >
                <div className="flex flex-wrap items-baseline gap-x-2 gap-y-1">
                  <span className="text-base font-semibold text-ink">{route.from}</span>
                  <span className="text-ink-tertiary" aria-hidden>
                    u
                  </span>
                  <span className="text-base font-semibold text-ink">{route.to}</span>
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  {route.transitionTypeId.map((typeId) => (
                    <span
                      key={typeId}
                      className="inline-flex items-center rounded-md border border-primary/30 bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary"
                    >
                      {typeNames.get(typeId) ?? typeId}
                    </span>
                  ))}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
