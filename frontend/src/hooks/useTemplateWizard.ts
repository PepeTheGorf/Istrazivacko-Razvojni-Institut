import { useState, /*useCallback*/ } from 'react'
import { createSmartTemplate } from '../api/smartDocs'
import type { TemplateCreationPayload } from '../types/smartDocs'

export function useTemplateWizard(onSuccess: () => void) {
  const [step, setStep] = useState(1)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  // Osnovni podaci
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  // Oblast i Tip
  const [domainId, setDomainId] = useState<number | undefined>()
  const [newDomain, setNewDomain] = useState('')
  const [categoryId, setCategoryId] = useState<number | undefined>()
  const [newCategory, setNewCategory] = useState('')

  // Sekcije
  const [sections, setSections] = useState<Array<{ title: string; systemPrompt: string; order: number }>>([
    { title: '', systemPrompt: '', order: 1 }
  ])

  const addSection = () => {
    setSections([...sections, { title: '', systemPrompt: '', order: sections.length + 1 }])
  }

  const removeSection = (index: number) => {
    setSections(sections.filter((_, i) => i !== index))
  }

  const updateSection = (index: number, field: 'title' | 'systemPrompt', value: string) => {
    const newSections = [...sections]
    newSections[index][field] = value
    setSections(newSections)
  }

  const handleSave = async () => {
    if (!name || (sections.some(s => !s.title || !s.systemPrompt))) {
      setError('Molimo popunite sva obavezna polja i promptove.')
      return
    }

    setLoading(true)
    setError(null)

    const payload: TemplateCreationPayload = {
      name,
      description,
      domainId: domainId || undefined,
      newDomain: newDomain || undefined,
      categoryId: categoryId || undefined,
      newCategory: newCategory || undefined,
      sections: sections.map((s, i) => ({ ...s, order: i + 1 }))
    }

    try {
      await createSmartTemplate(payload)
      onSuccess()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Greška pri čuvanju šablona')
    } finally {
      setLoading(false)
    }
  }

  return {
    step, setStep, name, setName, description, setDescription,
    domainId, setDomainId, newDomain, setNewDomain,
    categoryId, setCategoryId, newCategory, setNewCategory,
    sections, addSection, removeSection, updateSection,
    handleSave, error, loading
  }
}