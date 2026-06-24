import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../../../auth/AuthContext'
import { deleteProject, fetchProjects } from '../../../api/projects'
import { fetchProjekatPristupIds } from '../../../api/pristup'
import type { Project } from '../../../types/project'

export function useProjectsPage() {
  const { user } = useAuth()
  const canManage = user?.role === 'MANAGER'

  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [projectToDelete, setProjectToDelete] = useState<Project | null>(null)
  const [deleting, setDeleting] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const list = await fetchProjects()

      if (user?.role === 'TEAM_MEMBER' && user.id != null) {
        const allIds = list.map((p) => String(p.id)).filter(Boolean)
        if (allIds.length > 0) {
          const allowedIds = await fetchProjekatPristupIds(String(user.id), allIds)
          const allowedSet = new Set(allowedIds)
          setProjects(list.filter((p) => allowedSet.has(String(p.id))))
        } else {
          setProjects([])
        }
      } else {
        setProjects(list)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje projekata nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [user?.role, user?.id])

  useEffect(() => {
    void load()
  }, [load])

  async function confirmDelete() {
    if (!projectToDelete?.id) {
      setError('Nije moguće obrisati projekat bez ID-ja.')
      setProjectToDelete(null)
      return
    }

    if (!canManage) {
      setError('Nemate dozvolu za brisanje projekta.')
      setProjectToDelete(null)
      return
    }

    setDeleting(true)
    setError(null)
    try {
      await deleteProject(String(projectToDelete.id))
      setProjects((prev) => prev.filter((p) => p.id !== projectToDelete.id))
      setProjectToDelete(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Brisanje projekta nije uspelo')
    } finally {
      setDeleting(false)
    }
  }

  return {
    projects,
    loading,
    error,
    load,
    projectToDelete,
    setProjectToDelete,
    deleting,
    confirmDelete,
    canManage,
  }
}
