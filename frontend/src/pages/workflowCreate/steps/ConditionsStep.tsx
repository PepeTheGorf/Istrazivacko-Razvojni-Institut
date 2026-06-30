import { useCallback, useEffect, useMemo, useState } from 'react'
import { fetchTransitionConditionTypes } from '../../../api/workflows'
import { Button } from '../../../components/ui/Button'
import { SelectField } from '../../../components/ui/SelectField'
import { TransitionConditionTypesModal } from '../../../components/workflow/TransitionConditionTypesModal'
import { cn } from '../../../lib/cn'
import { findRoute, routeExists, routeKey } from '../../../lib/transitionRoutes'
import type { PhaseCreation, TransitionConditionCreation, TransitionConditionType } from '../../../types/workflow'

interface ConditionsStepProps {
  phases: PhaseCreation[]
  transitionConditions: TransitionConditionCreation[]
  onCreateTransition: (condition: TransitionConditionCreation) => void
  onAddTypeToRoute: (from: string, to: string, typeId: number) => void
  onRemoveTypeFromRoute: (from: string, to: string, typeId: number) => void
  onRemoveTransitionRoute: (from: string, to: string) => void
  onReplaceTransition: (
    originalFrom: string,
    originalTo: string,
    updated: TransitionConditionCreation,
  ) => void
}

function ChevronIcon({ expanded }: { expanded: boolean }) {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      aria-hidden
      className={cn(
        'h-4 w-4 shrink-0 text-ink-subtle transition-transform duration-250 ease-out',
        expanded ? 'rotate-90' : 'rotate-0',
      )}
    >
      <path
        d="M6 4L10 8L6 12"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function ConditionsStep({
  phases,
  transitionConditions,
  onCreateTransition,
  onAddTypeToRoute,
  onRemoveTypeFromRoute,
  onRemoveTransitionRoute,
  onReplaceTransition,
}: ConditionsStepProps) {
  const [types, setTypes] = useState<TransitionConditionType[]>([])
  const [typesLoading, setTypesLoading] = useState(true)
  const [typesError, setTypesError] = useState<string | null>(null)
  const [catalogOpen, setCatalogOpen] = useState(false)
  const [collapsedRoutes, setCollapsedRoutes] = useState<Set<string>>(new Set())
  const [editingOriginal, setEditingOriginal] = useState<{ from: string; to: string } | null>(
    null,
  )
  const [fromPhase, setFromPhase] = useState('')
  const [toPhase, setToPhase] = useState('')
  const [formTypeIds, setFormTypeIds] = useState<number[]>([])
  const [transitionTypeId, setTransitionTypeId] = useState('')
  const [formError, setFormError] = useState<string | null>(null)

  const phaseOptions = useMemo(
    () => phases.map((p) => p.name.trim()).filter((name) => name.length > 0),
    [phases],
  )

  const toPhaseOptions = useMemo(
    () => phaseOptions.filter((name) => name !== fromPhase),
    [fromPhase, phaseOptions],
  )

  const typeById = useMemo(() => new Map(types.map((t) => [t.id, t])), [types])
  const selectedType = transitionTypeId ? typeById.get(Number(transitionTypeId)) : undefined
  const routeReady = Boolean(fromPhase && toPhase)
  const existingRoute = routeReady
    ? findRoute(transitionConditions, fromPhase, toPhase)
    : undefined
  const isEditing = editingOriginal !== null

  const takenTypeIds = useMemo(() => {
    if (isEditing) return formTypeIds
    if (existingRoute && routeReady && !isEditing) return existingRoute.transitionTypeId
    return formTypeIds
  }, [existingRoute, formTypeIds, isEditing, routeReady])

  const availableTypesForDropdown = useMemo(() => {
    return types.filter((t) => !takenTypeIds.includes(t.id))
  }, [takenTypeIds, types])

  const showFormTypeChips = isEditing || (!existingRoute && formTypeIds.length > 0)

  const resetForm = useCallback(() => {
    setFromPhase('')
    setToPhase('')
    setFormTypeIds([])
    setTransitionTypeId('')
    setFormError(null)
    setEditingOriginal(null)
  }, [])

  const loadRouteIntoForm = useCallback((route: TransitionConditionCreation) => {
    setFromPhase(route.from)
    setToPhase(route.to)
    setFormTypeIds([...route.transitionTypeId])
    setTransitionTypeId('')
    setFormError(null)
    setEditingOriginal({ from: route.from, to: route.to })
  }, [])

  useEffect(() => {
    let cancelled = false
    setTypesLoading(true)
    setTypesError(null)
    void fetchTransitionConditionTypes()
      .then((data) => {
        if (!cancelled) setTypes(data)
      })
      .catch((err) => {
        if (!cancelled) {
          setTypesError(
            err instanceof Error ? err.message : 'Učitavanje tipova uslova nije uspelo',
          )
        }
      })
      .finally(() => {
        if (!cancelled) setTypesLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (phaseOptions.length === 0) {
      resetForm()
      return
    }
    if (fromPhase && !phaseOptions.includes(fromPhase)) resetForm()
    if (toPhase && !phaseOptions.includes(toPhase)) setToPhase('')
    if (fromPhase && toPhase === fromPhase) setToPhase('')
  }, [fromPhase, phaseOptions, resetForm, toPhase])

  const toggleRouteCollapsed = useCallback((key: string) => {
    setCollapsedRoutes((prev) => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })
  }, [])

  const handleFromChange = useCallback(
    (value: string) => {
      setFromPhase(value)
      setFormError(null)
      setToPhase((prev) => (prev === value ? '' : prev))
      if (!isEditing) {
        setFormTypeIds([])
      }
      setTransitionTypeId('')
    },
    [isEditing],
  )

  const handleToChange = useCallback(
    (value: string) => {
      setFormError(null)
      setToPhase(value)
      if (!isEditing) {
        setFormTypeIds([])
      }
      setTransitionTypeId('')
    },
    [isEditing],
  )

  const handleAddTypeToForm = useCallback(() => {
    if (!transitionTypeId) {
      setFormError('Izaberite tip uslova.')
      return
    }
    if (takenTypeIds.includes(Number(transitionTypeId))) {
      setFormError('Ovaj tip uslova je već dodat.')
      return
    }

    if (existingRoute && routeReady && !isEditing) {
      onAddTypeToRoute(fromPhase, toPhase, Number(transitionTypeId))
      setTransitionTypeId('')
      setFormError(null)
      return
    }

    setFormTypeIds((prev) => [...prev, Number(transitionTypeId)])
    setTransitionTypeId('')
    setFormError(null)
  }, [
    existingRoute,
    fromPhase,
    isEditing,
    onAddTypeToRoute,
    routeReady,
    takenTypeIds,
    toPhase,
    transitionTypeId,
  ])

  const handleCreateCondition = useCallback(() => {
    if (!fromPhase || !toPhase) {
      setFormError('Izaberite početnu i krajnju fazu.')
      return
    }
    if (routeExists(transitionConditions, fromPhase, toPhase)) {
      setFormError('Prelaz za ovaj par faza već postoji. Koristite "Dodaj tip uslova".')
      return
    }
    if (formTypeIds.length === 0) {
      setFormError('Dodajte bar jedan tip uslova pre kreiranja.')
      return
    }
    onCreateTransition({
      from: fromPhase,
      to: toPhase,
      transitionTypeId: [...formTypeIds],
    })
    resetForm()
  }, [formTypeIds, fromPhase, onCreateTransition, resetForm, toPhase, transitionConditions])

  const handleSaveEdit = useCallback(() => {
    if (!editingOriginal || !fromPhase || !toPhase) return
    if (formTypeIds.length === 0) {
      setFormError('Prelaz mora imati bar jedan tip uslova.')
      return
    }
    if (
      routeExists(transitionConditions, fromPhase, toPhase, {
        from: editingOriginal.from,
        to: editingOriginal.to,
      })
    ) {
      setFormError('Prelaz sa istim parom faza već postoji.')
      return
    }
    onReplaceTransition(editingOriginal.from, editingOriginal.to, {
      from: fromPhase,
      to: toPhase,
      transitionTypeId: [...formTypeIds],
    })
    resetForm()
  }, [
    editingOriginal,
    formTypeIds,
    fromPhase,
    onReplaceTransition,
    resetForm,
    toPhase,
    transitionConditions,
  ])

  const handleRemoveFormType = useCallback((typeId: number) => {
    setFormTypeIds((prev) => prev.filter((id) => id !== typeId))
    setFormError(null)
  }, [])

  return (
    <div className="grid w-full gap-6 py-2">
      <div className="sticky top-14 z-[9] -mx-1 border-b border-hairline bg-canvas/95 px-1 pb-4 backdrop-blur-sm">
        <div className="flex flex-wrap items-center justify-end gap-3 pt-1">
          <Button
            type="button"
            variant="secondary"
            onClick={() => setCatalogOpen(true)}
            disabled={typesLoading}
            className="shrink-0"
          >
            Pregled svih tipova uslova
          </Button>
        </div>
      </div>

      {typesError && (
        <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
          {typesError}
        </p>
      )}

      <section
        className={cn(
          'rounded-lg border bg-surface-2 p-5 md:p-6',
          isEditing ? 'border-primary/40' : 'border-hairline',
        )}
      >
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h3 className="m-0 text-base font-semibold text-ink">
              {isEditing ? 'Izmeni uslov prelaza' : 'Novi uslov prelaza'}
            </h3>
            <p className="m-0 mt-1 text-sm text-ink-subtle">
              {isEditing
                ? 'Izmenite faze i tipove uslova, zatim sačuvajte.'
                : 'Izaberite faze, dodajte tipove, pa kreirajte prelaz u listi.'}
            </p>
          </div>
          {isEditing && (
            <Button type="button" variant="secondary" className="min-h-8 text-xs" onClick={resetForm}>
              Otkaži izmenu
            </Button>
          )}
        </div>

        <div className="mt-5 grid gap-4 sm:grid-cols-2">
          <SelectField
            label="Iz Faze"
            name="transition-from"
            value={fromPhase}
            onChange={(e) => handleFromChange(e.target.value)}
          >
            <option value="">Izaberite fazu...</option>
            {phaseOptions.map((name) => (
              <option key={name} value={name}>
                {name}
              </option>
            ))}
          </SelectField>

          <SelectField
            label="U Fazu"
            name="transition-to"
            value={toPhase}
            disabled={!fromPhase}
            onChange={(e) => handleToChange(e.target.value)}
          >
            <option value="">Izaberite fazu...</option>
            {toPhaseOptions.map((name) => (
              <option key={name} value={name}>
                {name}
              </option>
            ))}
          </SelectField>
        </div>

        {routeReady && existingRoute && !isEditing && (
          <p className="m-0 mt-3 rounded-md border border-primary/30 bg-primary/10 px-3 py-2 text-sm text-ink-muted">
            Prelaz{' '}
            <span className="font-medium text-ink">
              {fromPhase} u {toPhase}
            </span>{' '}
            već postoji. "Dodaj tip uslova" dodaje na postojeći prelaz.
          </p>
        )}

        {routeReady && (
          <div className="mt-4 grid gap-4">
            <SelectField
              label="Tip uslova"
              name="transition-type"
              value={transitionTypeId}
              disabled={typesLoading || availableTypesForDropdown.length === 0}
              onChange={(e) => {
                setTransitionTypeId(e.target.value)
                setFormError(null)
              }}
            >
              <option value="">
                {availableTypesForDropdown.length === 0
                  ? 'Svi tipovi su već dodati'
                  : 'Izaberite tip...'}
              </option>
              {availableTypesForDropdown.map((type) => (
                <option key={type.id} value={String(type.id)}>
                  {type.name}
                </option>
              ))}
            </SelectField>

            {selectedType && (
              <div className="rounded-md border border-hairline bg-surface-1 px-4 py-3">
                <p className="m-0 text-sm font-medium text-ink">{selectedType.name}</p>
                <p className="m-0 mt-1.5 text-sm leading-relaxed text-ink-muted">
                  {selectedType.description}
                </p>
              </div>
            )}

            {showFormTypeChips && (
              <div>
                <p className="m-0 mb-2 text-[13px] font-medium text-ink-muted">
                  {isEditing ? 'Tipovi u izmeni' : 'Tipovi za novi prelaz'}
                </p>
                <ul className="m-0 flex flex-wrap gap-2 p-0 list-none">
                  {formTypeIds.map((typeId) => {
                    const type = typeById.get(typeId)
                    return (
                      <li
                        key={typeId}
                        className="inline-flex items-center gap-2 rounded-md border border-primary/25 bg-primary/10 px-2.5 py-1"
                      >
                        <span className="text-xs font-medium text-primary">
                          {type?.name ?? typeId}
                        </span>
                        <button
                          type="button"
                          aria-label="Ukloni tip"
                          className="text-xs text-ink-subtle hover:text-error"
                          onClick={() => handleRemoveFormType(typeId)}
                        >
                          ×
                        </button>
                      </li>
                    )
                  })}
                </ul>
              </div>
            )}
          </div>
        )}

        {formError && <p className="m-0 mt-3 text-sm text-error">{formError}</p>}

        <div className="mt-5 flex flex-wrap justify-end gap-2 border-t border-hairline pt-4">
          {routeReady && (
            <Button
              type="button"
              variant="secondary"
              disabled={!transitionTypeId}
              onClick={handleAddTypeToForm}
            >
              Dodaj tip uslova
            </Button>
          )}
          {isEditing ? (
            <Button type="button" variant="secondary" onClick={handleSaveEdit}>
              Sačuvaj izmene
            </Button>
          ) : (
            <Button
              type="button"
              variant="secondary"
              icon="add"
              disabled={!routeReady || existingRoute !== undefined || formTypeIds.length === 0}
              onClick={handleCreateCondition}
            >
              Kreiraj uslov
            </Button>
          )}
        </div>
      </section>

      <section>
        <h3 className="m-0 text-xs font-semibold tracking-wider text-ink-subtle uppercase">
          Definisani prelazi ({transitionConditions.length})
        </h3>

        {transitionConditions.length === 0 ? (
          <p className="m-0 mt-3 rounded-lg border border-dashed border-hairline bg-surface-2 px-4 py-8 text-center text-sm text-ink-subtle">
            Lista je prazna. Popunite formu iznad i kliknite Kreiraj uslov.
          </p>
        ) : (
          <div className="mt-3 grid gap-3">
            {transitionConditions.map((route) => {
              const key = routeKey(route.from, route.to)
              const isExpanded = !collapsedRoutes.has(key)
              return (
                <article
                  key={key}
                  className="overflow-hidden rounded-lg border border-hairline bg-surface-2"
                >
                  <div className="flex flex-wrap items-center gap-2 border-b border-hairline bg-surface-3 px-3 py-2.5">
                    <button
                      type="button"
                      aria-expanded={isExpanded}
                      aria-label={isExpanded ? 'Skupi prelaz' : 'Proširi prelaz'}
                      className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-md border border-transparent text-ink-muted transition-colors hover:border-hairline hover:bg-surface-2 hover:text-ink"
                      onClick={() => toggleRouteCollapsed(key)}
                    >
                      <ChevronIcon expanded={isExpanded} />
                    </button>
                    <button
                      type="button"
                      className="flex min-w-0 flex-1 flex-wrap items-center gap-2 text-left text-sm"
                      onClick={() => toggleRouteCollapsed(key)}
                    >
                      <span className="font-semibold text-ink">{route.from}</span>
                      <span className="text-ink-tertiary" aria-hidden>
                        u
                      </span>
                      <span className="font-semibold text-ink">{route.to}</span>
                    </button>
                    <div className="flex flex-wrap gap-2">
                      <Button
                        type="button"
                        variant="secondary"
                        icon="edit"
                        className="min-h-8 px-2.5 py-1 text-xs"
                        onClick={() => loadRouteIntoForm(route)}
                      >
                        Izmeni
                      </Button>
                      <Button
                        type="button"
                        variant="delete"
                        icon="delete"
                        className="min-h-8 border border-error/45 px-2.5 py-1 text-xs"
                        onClick={() => {
                          if (
                            editingOriginal?.from === route.from &&
                            editingOriginal?.to === route.to
                          ) {
                            resetForm()
                          }
                          onRemoveTransitionRoute(route.from, route.to)
                        }}
                      >
                        Ukloni
                      </Button>
                    </div>
                  </div>

                  <div
                    className={cn(
                      'overflow-hidden transition-all duration-250 ease-out',
                      isExpanded ? 'max-h-[2000px] opacity-100' : 'max-h-0 opacity-0',
                    )}
                  >
                    <ul className="m-0 grid list-none gap-0 p-0">
                      {route.transitionTypeId.map((typeId) => {
                        const type = typeById.get(typeId)
                        return (
                          <li
                            key={typeId}
                            className="flex flex-wrap items-center justify-between gap-3 border-b border-hairline px-4 py-3 last:border-b-0"
                          >
                            <div className="min-w-0">
                              <span className="inline-block rounded-md border border-primary/25 bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
                                {type?.name ?? typeId}
                              </span>
                              {type?.description ? (
                                <p className="m-0 mt-1.5 text-xs text-ink-subtle">
                                  {type.description}
                                </p>
                              ) : null}
                            </div>
                            <Button
                              type="button"
                              variant="delete"
                              icon="delete"
                              className="min-h-8 shrink-0 border border-error/45 px-2.5 py-1 text-xs"
                              onClick={() =>
                                onRemoveTypeFromRoute(route.from, route.to, typeId)
                              }
                            >
                              Ukloni tip
                            </Button>
                          </li>
                        )
                      })}
                    </ul>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>

      <TransitionConditionTypesModal
        open={catalogOpen}
        types={types}
        loading={typesLoading}
        onClose={() => setCatalogOpen(false)}
      />
    </div>
  )
}
