import { useCallback, useEffect, useMemo, useState } from 'react'
import { fetchProjectTeamMemberStats, fetchProjectWorkflowAnalytics } from '../../../api/analytics'
import { fetchProjects } from '../../../api/projects'
import { fetchTasksByProject } from '../../../api/tasks'
import { toIsoDateTimeOrUndefined } from '../../../lib/datetimeInput'
import { buildGrafanaDashboardUrl } from '../../../lib/grafana'
import type {
  AnalyticsFilters,
  AnalyticsStatisticType,
  ProjectWorkflowAnalysis,
  TaskTeamMemberStats,
} from '../../../types/analytics'
import { emptyAnalyticsFilters } from '../../../types/analytics'
import type { Project } from '../../../types/project'
import type { TaskSummary } from '../../../types/task'
import { flattenTasks } from '../lib/workflowTaskUtils'

function toApiFilters(filters: AnalyticsFilters): Partial<AnalyticsFilters> {
  return {
    from: toIsoDateTimeOrUndefined(filters.from),
    to: toIsoDateTimeOrUndefined(filters.to),
    memberId: filters.memberId || undefined,
    taskId: filters.taskId || undefined,
  }
}

export function useManagerAnalyticsPage() {
  const [projects, setProjects] = useState<Project[]>([])
  const [selectedProjectId, setSelectedProjectId] = useState('')
  const [statistic, setStatistic] = useState<AnalyticsStatisticType>('workflow')
  const [filters, setFilters] = useState<AnalyticsFilters>(emptyAnalyticsFilters)
  const [workflow, setWorkflow] = useState<ProjectWorkflowAnalysis | null>(null)
  const [projectTasks, setProjectTasks] = useState<TaskSummary[]>([])
  const [teamStats, setTeamStats] = useState<TaskTeamMemberStats[]>([])
  const [memberOptions, setMemberOptions] = useState<TaskTeamMemberStats[]>([])
  const [loadingProjects, setLoadingProjects] = useState(true)
  const [loadingOptions, setLoadingOptions] = useState(false)
  const [loadingAnalytics, setLoadingAnalytics] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadProjects() {
      setLoadingProjects(true)
      setError(null)
      try {
        const data = await fetchProjects()
        if (cancelled) return
        setProjects(data)
        if (data.length > 0) {
          setSelectedProjectId(String(data[0].id))
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Učitavanje projekata nije uspelo')
        }
      } finally {
        if (!cancelled) {
          setLoadingProjects(false)
        }
      }
    }

    void loadProjects()

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (!selectedProjectId) return

    let cancelled = false

    async function loadFilterOptions() {
      setLoadingOptions(true)
      try {
        const [members, tasks] = await Promise.all([
          fetchProjectTeamMemberStats(selectedProjectId),
          fetchTasksByProject(selectedProjectId),
        ])
        if (cancelled) return
        setMemberOptions(members)
        setProjectTasks(tasks)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Učitavanje filtera nije uspelo')
        }
      } finally {
        if (!cancelled) {
          setLoadingOptions(false)
        }
      }
    }

    setFilters(emptyAnalyticsFilters)
    void loadFilterOptions()

    return () => {
      cancelled = true
    }
  }, [selectedProjectId])

  const apiFilters = useMemo(() => toApiFilters(filters), [filters])

  const loadAnalytics = useCallback(async () => {
    if (!selectedProjectId) return

    setLoadingAnalytics(true)
    setError(null)
    try {
      if (statistic === 'workflow') {
        const data = await fetchProjectWorkflowAnalytics(selectedProjectId, apiFilters)
        setWorkflow(data)
        setTeamStats([])
      } else {
        const data = await fetchProjectTeamMemberStats(selectedProjectId, apiFilters)
        setTeamStats(data)
        setWorkflow(null)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje analitike nije uspelo')
    } finally {
      setLoadingAnalytics(false)
    }
  }, [selectedProjectId, statistic, apiFilters])

  useEffect(() => {
    void loadAnalytics()
  }, [loadAnalytics])

  const taskOptions = useMemo(() => flattenTasks(projectTasks), [projectTasks])

  const grafanaDashboardUrl = useMemo(() => {
    if (!selectedProjectId) return null
    return buildGrafanaDashboardUrl(selectedProjectId, statistic)
  }, [selectedProjectId, statistic])

  const selectedProject = projects.find((project) => String(project.id) === selectedProjectId) ?? null

  function updateFilter<K extends keyof AnalyticsFilters>(key: K, value: AnalyticsFilters[K]) {
    setFilters((prev) => ({ ...prev, [key]: value }))
  }

  function clearFilters() {
    setFilters(emptyAnalyticsFilters)
  }

  const hasActiveFilters =
    Boolean(filters.from) ||
    Boolean(filters.to) ||
    Boolean(filters.memberId) ||
    Boolean(filters.taskId)

  return {
    projects,
    selectedProjectId,
    setSelectedProjectId,
    selectedProject,
    statistic,
    setStatistic,
    filters,
    updateFilter,
    clearFilters,
    hasActiveFilters,
    memberOptions,
    taskOptions,
    workflow,
    projectTasks,
    teamStats,
    loadingProjects,
    loadingOptions,
    loadingAnalytics,
    error,
    grafanaDashboardUrl,
    reloadAnalytics: loadAnalytics,
  }
}
