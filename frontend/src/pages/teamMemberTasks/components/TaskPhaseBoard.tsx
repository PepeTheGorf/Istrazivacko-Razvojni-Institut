import { useMemo, useState } from 'react'
import { moveTaskToPhase } from '../../../api/tasks'
import { Button } from '../../../components/ui/Button'
import { SelectField } from '../../../components/ui/SelectField'
import { cn } from '../../../lib/cn'
import type { TaskPhaseTransition, TaskTransitionsResponse } from '../../../types/task'

interface TaskPhaseBoardProps {
  taskId: string
  transitionsData: TaskTransitionsResponse | null
  loading: boolean
  onMoved: () => void
}

function transitionForPhase(
  transitions: TaskPhaseTransition[],
  phaseId: number,
): TaskPhaseTransition | undefined {
  return transitions.find((item) => item.toPhaseId === phaseId)
}

export function TaskPhaseBoard({
  taskId,
  transitionsData,
  loading,
  onMoved,
}: TaskPhaseBoardProps) {
  const [selectedPhaseId, setSelectedPhaseId] = useState('')
  const [moving, setMoving] = useState(false)
  const [message, setMessage] = useState<{ type: 'error' | 'success'; text: string } | null>(null)

  const currentPhaseId = transitionsData?.currentPhaseId
  const currentPhaseName = transitionsData?.currentPhaseName
  const transitions = transitionsData?.transitions ?? []

  const targetPhases = useMemo(
    () =>
      [...(transitionsData?.workflowPhases ?? [])]
        .filter((phase) => phase.id != null && phase.id !== currentPhaseId)
        .sort((a, b) => (a.order ?? 0) - (b.order ?? 0)),
    [transitionsData?.workflowPhases, currentPhaseId],
  )

  const selectedTransition = useMemo(() => {
    if (!selectedPhaseId) return null
    return transitionForPhase(transitions, Number(selectedPhaseId))
  }, [selectedPhaseId, transitions])

  async function handleMove() {
    if (!selectedPhaseId) {
      setMessage({ type: 'error', text: 'Izaberite fazu u koju želite da prebacite zadatak.' })
      return
    }

    const phaseId = Number(selectedPhaseId)
    const phase = targetPhases.find((item) => item.id === phaseId)
    const transition = transitionForPhase(transitions, phaseId)

    if (!transition?.routeExists) {
      setMessage({
        type: 'error',
        text: `Nema definisanog prelaza iz "${currentPhaseName ?? 'trenutne faze'}" u "${phase?.name ?? 'izabranu fazu'}".`,
      })
      return
    }

    if (!transition.conditionsMet) {
      const unmet = transition.requirements.filter((item) => item.met !== true).map((item) => item.name)
      setMessage({
        type: 'error',
        text: unmet.length
          ? `Uslovi prelaza nisu ispunjeni: ${unmet.join(', ')}.`
          : 'Uslovi prelaza nisu ispunjeni.',
      })
      return
    }

    setMoving(true)
    setMessage(null)
    try {
      await moveTaskToPhase(taskId, phaseId)
      setMessage({ type: 'success', text: `Zadatak je premešten u fazu "${phase?.name}".` })
      setSelectedPhaseId('')
      onMoved()
    } catch (err) {
      setMessage({
        type: 'error',
        text: err instanceof Error ? err.message : 'Premeštanje zadatka nije uspelo.',
      })
    } finally {
      setMoving(false)
    }
  }

  if (loading) {
    return (
      <section className="rounded-xl border border-hairline bg-surface-1 p-4">
        <p className="m-0 text-sm text-ink-subtle">Učitavanje faza...</p>
      </section>
    )
  }

  if ((transitionsData?.workflowPhases?.length ?? 0) === 0) {
    return (
      <section className="rounded-xl border border-hairline bg-surface-1 p-4">
        <h2 className="m-0 text-base font-semibold text-ink">Promena faze</h2>
        <p className="m-0 mt-2 text-sm text-ink-subtle">
          Zadatak nema dodeljen radni tok ili faze nisu definisane.
        </p>
      </section>
    )
  }

  return (
    <section className="rounded-xl border border-hairline bg-surface-1 p-4">
      <h2 className="m-0 text-base font-semibold text-ink">Promena faze</h2>
      <p className="m-0 mt-1 text-sm text-ink-subtle">
        Trenutna faza: <span className="text-ink">{currentPhaseName ?? 'Nepoznata'}</span>
      </p>

      <div className="mt-4 grid gap-3 md:grid-cols-[minmax(0,1fr)_auto] md:items-end">
        <SelectField
          label="Premesti u fazu"
          name="targetPhaseId"
          value={selectedPhaseId}
          disabled={moving || targetPhases.length === 0}
          onChange={(event) => {
            setSelectedPhaseId(event.target.value)
            setMessage(null)
          }}
        >
          <option value="">
            {targetPhases.length === 0 ? 'Nema drugih faza' : 'Izaberite fazu'}
          </option>
          {targetPhases.map((phase) => (
            <option key={phase.id} value={phase.id}>
              {phase.name}
            </option>
          ))}
        </SelectField>
        <Button
          type="button"
          disabled={moving || !selectedPhaseId}
          onClick={() => void handleMove()}
        >
          {moving ? 'Premeštanje...' : 'Premesti'}
        </Button>
      </div>

      {message ? (
        <p
          className={cn(
            'm-0 mt-3 rounded-md border px-3 py-2 text-sm',
            message.type === 'error'
              ? 'border-error/35 bg-error/10 text-[#ffb4b4]'
              : 'border-primary/35 bg-primary/10 text-primary-hover',
          )}
        >
          {message.text}
        </p>
      ) : null}

      {selectedTransition ? (
        <div className="mt-4 rounded-md border border-hairline bg-surface-2 p-3">
          <p className="m-0 text-xs font-medium tracking-wide text-ink-muted uppercase">
            Uslovi za izabrani prelaz
          </p>
          {!selectedTransition.routeExists ? (
            <p className="m-0 mt-2 text-sm text-ink-subtle">
              Prelaz nije definisan u radnom toku.
            </p>
          ) : selectedTransition.requirements.length === 0 ? (
            <p className="m-0 mt-2 text-sm text-ink-subtle">Nema dodatnih uslova.</p>
          ) : (
            <ul className="m-0 mt-2 list-none space-y-1.5 p-0">
              {selectedTransition.requirements.map((requirement) => (
                <li
                  key={requirement.id}
                  className="flex items-start justify-between gap-3 rounded-md border border-hairline bg-surface-1 px-3 py-2 text-sm"
                >
                  <span className="text-ink">{requirement.name}</span>
                  <span
                    className={cn(
                      'shrink-0 text-xs font-medium',
                      requirement.met === true ? 'text-semantic-success' : 'text-error',
                    )}
                  >
                    {requirement.met === true ? 'Ispunjeno' : 'Nije ispunjeno'}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      ) : null}

      {transitions.length > 0 ? (
        <div className="mt-4 border-t border-hairline pt-4">
          <p className="m-0 text-xs font-medium tracking-wide text-ink-muted uppercase">
            Svi mogući prelazi iz trenutne faze
          </p>
          <ul className="m-0 mt-2 list-none space-y-2 p-0">
            {transitions.map((transition) => (
              <li
                key={transition.toPhaseId}
                className="rounded-md border border-hairline bg-surface-2 px-3 py-2"
              >
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <span className="text-sm text-ink">U fazu: {transition.toPhaseName}</span>
                  <span className="text-xs text-ink-subtle">
                    {!transition.routeExists
                      ? 'Nema prelaza'
                      : transition.conditionsMet
                        ? 'Spremno'
                        : 'Uslovi nisu ispunjeni'}
                  </span>
                </div>
                {transition.routeExists && transition.requirements.length > 0 ? (
                  <p className="m-0 mt-1 text-xs text-ink-tertiary">
                    {transition.requirements.map((item) => item.name).join(' · ')}
                  </p>
                ) : null}
              </li>
            ))}
          </ul>
        </div>
      ) : null}
    </section>
  )
}
