import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createTechnicalResource,
  deleteTechnicalResource,
  fetchTechnicalResources,
  updateTechnicalResource,
} from '../../../api/technicalResources'
import type { TechnicalResource, TechnicalResourcePayload } from '../../../types/technicalResource'

export function useTechnicalResourcesPage() {
  const [resources, setResources] = useState<TechnicalResource[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [resourceToDelete, setResourceToDelete] = useState<TechnicalResource | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [formOpen, setFormOpen] = useState(false)
  const [editingResource, setEditingResource] = useState<TechnicalResource | null>(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setResources(await fetchTechnicalResources())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje resursa nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return resources
    return resources.filter(
      (item) =>
        item.name.toLowerCase().includes(q) ||
        (item.description?.toLowerCase().includes(q) ?? false),
    )
  }, [resources, search])

  function openCreate() {
    setEditingResource(null)
    setFormOpen(true)
  }

  function openEdit(resource: TechnicalResource) {
    setEditingResource(resource)
    setFormOpen(true)
  }

  async function saveResource(payload: TechnicalResourcePayload) {
    setSaving(true)
    setError(null)
    try {
      if (editingResource?.id) {
        await updateTechnicalResource(editingResource.id, payload)
      } else {
        await createTechnicalResource(payload)
      }
      setFormOpen(false)
      setEditingResource(null)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Čuvanje resursa nije uspelo')
      throw err
    } finally {
      setSaving(false)
    }
  }

  async function confirmDelete() {
    if (!resourceToDelete?.id) return
    setDeleting(true)
    setError(null)
    try {
      await deleteTechnicalResource(resourceToDelete.id)
      setResourceToDelete(null)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Brisanje resursa nije uspelo')
    } finally {
      setDeleting(false)
    }
  }

  return {
    loading,
    error,
    search,
    setSearch,
    filtered,
    formOpen,
    setFormOpen,
    editingResource,
    openCreate,
    openEdit,
    saving,
    saveResource,
    resourceToDelete,
    setResourceToDelete,
    deleting,
    confirmDelete,
  }
}
