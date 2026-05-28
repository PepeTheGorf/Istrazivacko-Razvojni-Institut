import { useCallback, useEffect, useMemo, useState } from 'react'
import { deleteWorkflow, fetchWorkflows } from '../../../api/workflows'
import type { Workflow } from '../../../types/workflow'

export type WorkflowSortBy = 'name' | 'newest'

export function useWorkflowsPage() {
  const [workflows, setWorkflows] = useState<Workflow[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [sortBy, setSortBy] = useState<WorkflowSortBy>('name')
  const [workflowToDelete, setWorkflowToDelete] = useState<Workflow | null>(null)
  const [deleting, setDeleting] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const list = await fetchWorkflows()
      setWorkflows(list)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje tokova rada nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const id = setTimeout(() => {
      void load()
    }, 0)
    return () => clearTimeout(id)
  }, [load])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    let list = workflows
    if (q) {
      list = list.filter(
        (w) =>
          w.name.toLowerCase().includes(q) ||
          (w.description?.toLowerCase().includes(q) ?? false),
      )
    }
    return [...list].sort((a, b) => {
      if (sortBy === 'name') {
        return a.name.localeCompare(b.name, 'sr')
      }
      return (b.id ?? '').localeCompare(a.id ?? '')
    })
  }, [workflows, search, sortBy])

  async function confirmDelete() {
    if (!workflowToDelete?.id) {
      setError('Nije moguće obrisati tok bez ID-ja.')
      setWorkflowToDelete(null)
      return
    }
    setDeleting(true)
    setError(null)
    try {
      await deleteWorkflow(workflowToDelete.id)
      setWorkflows((prev) => prev.filter((w) => w.id !== workflowToDelete.id))
      setWorkflowToDelete(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Brisanje toka rada nije uspelo')
    } finally {
      setDeleting(false)
    }
  }

  return {
    workflows,
    loading,
    error,
    search,
    setSearch,
    sortBy,
    setSortBy,
    filtered,
    workflowToDelete,
    setWorkflowToDelete,
    deleting,
    confirmDelete,
  }
}
