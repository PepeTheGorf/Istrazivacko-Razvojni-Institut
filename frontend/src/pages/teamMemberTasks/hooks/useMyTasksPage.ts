import { useCallback, useEffect, useState } from 'react'
import { fetchMyAssignedProjects, fetchMyTasksByProject } from '../../../api/tasks'
import type { AssignedProjectSummary, AssignedTaskSummary } from '../../../types/task'

export function useMyTasksPage() {
  const [projects, setProjects] = useState<AssignedProjectSummary[]>([])
  const [selectedProjectId, setSelectedProjectId] = useState<string>('')
  const [tasks, setTasks] = useState<AssignedTaskSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [loadingTasks, setLoadingTasks] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadProjects = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const projectList = await fetchMyAssignedProjects()
      setProjects(projectList)
      setSelectedProjectId((current) => {
        if (current && projectList.some((project) => String(project.id) === current)) {
          return current
        }
        return projectList[0] ? String(projectList[0].id) : ''
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje projekata nije uspelo')
      setProjects([])
      setSelectedProjectId('')
    } finally {
      setLoading(false)
    }
  }, [])

  const loadTasks = useCallback(async (projectId: string) => {
    if (!projectId) {
      setTasks([])
      return
    }
    setLoadingTasks(true)
    setError(null)
    try {
      const taskList = await fetchMyTasksByProject(projectId)
      setTasks(taskList)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje zadataka nije uspelo')
      setTasks([])
    } finally {
      setLoadingTasks(false)
    }
  }, [])

  useEffect(() => {
    void loadProjects()
  }, [loadProjects])

  useEffect(() => {
    if (!selectedProjectId) {
      setTasks([])
      return
    }
    void loadTasks(selectedProjectId)
  }, [loadTasks, selectedProjectId])

  return {
    projects,
    selectedProjectId,
    setSelectedProjectId,
    tasks,
    loading,
    loadingTasks,
    error,
    reload: loadProjects,
  }
}
