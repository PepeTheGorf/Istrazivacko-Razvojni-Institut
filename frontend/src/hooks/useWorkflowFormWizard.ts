import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  WIZARD_TABS,
  type WizardTabKey,
} from '../components/workflow/workflowWizardConfig'
import type { WizardTabStatus } from '../components/workflow/WizardStepTabs'
import type {
  PhaseCreation,
  TransitionConditionCreation,
  WorkflowCreation,
} from '../types/workflow'

export interface WorkflowFormInitial {
  name?: string
  description?: string
  phases?: PhaseCreation[]
  transitionConditions?: TransitionConditionCreation[]
}

interface UseWorkflowFormWizardOptions {
  initial?: WorkflowFormInitial
  onCancel: () => void
  onSubmit: (workflow: WorkflowCreation) => Promise<void>
}

function nextTabLabel(tab: WizardTabKey): string {
  switch (tab) {
    case 'basic':
      return 'Dalje - Faze'
    case 'phases':
      return 'Dalje - Uslovi'
    case 'conditions':
      return 'Dalje - Pregled'
    default:
      return 'Sačuvaj'
  }
}

export function useWorkflowFormWizard({
  initial,
  onCancel,
  onSubmit,
}: UseWorkflowFormWizardOptions) {
  const [activeTab, setActiveTab] = useState<WizardTabKey>('basic')
  const [name, setName] = useState(initial?.name ?? '')
  const [description, setDescription] = useState(initial?.description ?? '')
  const [phases, setPhases] = useState<PhaseCreation[]>(initial?.phases ?? [])
  const [transitionConditions, setTransitionConditions] = useState<
    TransitionConditionCreation[]
  >(initial?.transitionConditions ?? [])
  const [newPhaseName, setNewPhaseName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    const phaseNames = new Set(
      phases.map((p) => p.name.trim()).filter((name) => name.length > 0),
    )
    setTransitionConditions((prev) =>
      prev.filter((t) => phaseNames.has(t.from) && phaseNames.has(t.to)),
    )
  }, [phases])

  const normalizedPhases = useMemo(() => {
    return phases
      .map((p, idx) => ({ ...p, order: idx + 1 }))
      .filter((p) => p.name.trim().length > 0)
  }, [phases])

  const tabIndex = WIZARD_TABS.indexOf(activeTab)

  const validateTab = useCallback(
    (tab: WizardTabKey): string | null => {
      if (tab === 'basic' && !name.trim()) {
        return 'Naziv toka rada je obavezan.'
      }
      if (tab === 'phases' && normalizedPhases.length < 2) {
        return 'Tok rada mora imati bar dve faze.'
      }
      return null
    },
    [name, normalizedPhases.length],
  )

  const getTabStatus = useCallback(
    (tab: WizardTabKey): WizardTabStatus => {
      const idx = WIZARD_TABS.indexOf(tab)
      const activeIdx = WIZARD_TABS.indexOf(activeTab)
      if (idx === activeIdx) return 'current'
      if (idx < activeIdx && validateTab(tab) === null) return 'completed'
      return 'upcoming'
    },
    [activeTab, validateTab],
  )

  const goBack = useCallback(() => {
    setError(null)
    if (tabIndex > 0) {
      setActiveTab(WIZARD_TABS[tabIndex - 1])
    } else {
      onCancel()
    }
  }, [onCancel, tabIndex])

  const handleSubmit = useCallback(async () => {
    const workflow: WorkflowCreation = {
      name: name.trim(),
      description: description.trim() || undefined,
      phases: normalizedPhases,
      transitionConditions:
        transitionConditions.length > 0 ? transitionConditions : undefined,
    }

    setSaving(true)
    setError(null)
    try {
      await onSubmit(workflow)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Čuvanje toka rada nije uspelo')
    } finally {
      setSaving(false)
    }
  }, [description, name, normalizedPhases, onSubmit, transitionConditions])

  const goNext = useCallback(() => {
    const message = validateTab(activeTab)
    if (message) {
      setError(message)
      return
    }
    setError(null)
    if (activeTab === 'review') {
      void handleSubmit()
      return
    }
    setActiveTab(WIZARD_TABS[tabIndex + 1])
  }, [activeTab, handleSubmit, tabIndex, validateTab])

  const handleTabSelect = useCallback(
    (tab: WizardTabKey) => {
      const targetIndex = WIZARD_TABS.indexOf(tab)
      if (targetIndex <= tabIndex) {
        setActiveTab(tab)
        setError(null)
        return
      }
      for (let i = 0; i < targetIndex; i++) {
        const message = validateTab(WIZARD_TABS[i])
        if (message) {
          setError(message)
          setActiveTab(WIZARD_TABS[i])
          return
        }
      }
      setError(null)
      setActiveTab(tab)
    },
    [tabIndex, validateTab],
  )

  const addPhase = useCallback(() => {
    const trimmed = newPhaseName.trim()
    if (!trimmed) return
    setPhases((prev) => [...prev, { name: trimmed, order: prev.length + 1 }])
    setNewPhaseName('')
  }, [newPhaseName])

  const movePhase = useCallback((index: number, direction: 'up' | 'down') => {
    setPhases((prev) => {
      const target = direction === 'up' ? index - 1 : index + 1
      if (target < 0 || target >= prev.length) return prev
      const next = [...prev]
      ;[next[index], next[target]] = [next[target], next[index]]
      return next
    })
  }, [])

  const removePhase = useCallback((index: number) => {
    setPhases((prev) => prev.filter((_, i) => i !== index))
  }, [])

  const createTransitionCondition = useCallback((condition: TransitionConditionCreation) => {
    setTransitionConditions((prev) => {
      if (prev.some((c) => c.from === condition.from && c.to === condition.to)) {
        return prev
      }
      return [...prev, condition]
    })
  }, [])

  const addTypeToRoute = useCallback((from: string, to: string, typeId: number) => {
    setTransitionConditions((prev) =>
      prev.map((c) => {
        if (c.from !== from || c.to !== to) return c
        if (c.transitionTypeId.includes(typeId)) return c
        return { ...c, transitionTypeId: [...c.transitionTypeId, typeId] }
      }),
    )
  }, [])

  const removeTypeFromRoute = useCallback((from: string, to: string, typeId: number) => {
    setTransitionConditions((prev) =>
      prev
        .map((c) => {
          if (c.from !== from || c.to !== to) return c
          const nextIds = c.transitionTypeId.filter((id) => id !== typeId)
          return { ...c, transitionTypeId: nextIds }
        })
        .filter((c) => c.transitionTypeId.length > 0),
    )
  }, [])

  const removeTransitionRoute = useCallback((from: string, to: string) => {
    setTransitionConditions((prev) =>
      prev.filter((c) => !(c.from === from && c.to === to)),
    )
  }, [])

  const replaceTransitionCondition = useCallback(
    (originalFrom: string, originalTo: string, updated: TransitionConditionCreation) => {
      setTransitionConditions((prev) => {
        const without = prev.filter(
          (c) => !(c.from === originalFrom && c.to === originalTo),
        )
        return [...without, updated]
      })
    },
    [],
  )

  const nextLabel =
    activeTab === 'review'
      ? saving
        ? 'Čuvanje...'
        : nextTabLabel('review')
      : nextTabLabel(activeTab)

  return {
    activeTab,
    name,
    description,
    phases,
    newPhaseName,
    error,
    saving,
    tabIndex,
    normalizedPhases,
    transitionConditions,
    createTransitionCondition,
    addTypeToRoute,
    removeTypeFromRoute,
    removeTransitionRoute,
    replaceTransitionCondition,
    getTabStatus,
    setName,
    setDescription,
    setNewPhaseName,
    goBack,
    goNext,
    handleTabSelect,
    addPhase,
    movePhase,
    removePhase,
    nextLabel,
  }
}
