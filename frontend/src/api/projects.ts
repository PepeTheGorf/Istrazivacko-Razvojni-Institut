import { apiFetch } from './client'
import type { Project } from '../types/project'

export function fetchProjects(): Promise<Project[]> {
  return apiFetch<Project[]>('/projects/all')
}

export function fetchProjectsForSelection(): Promise<Project[]> {
  return apiFetch<Project[]>('/projects/selection/all')
}

export function fetchProjectById(projectId: string): Promise<Project> {
  return apiFetch<Project>(`/projects/${projectId}`)
}

export function createProject(project: Project): Promise<void> {
  return apiFetch<void>('/projects', {
    method: 'POST',
    body: JSON.stringify(project),
  })
}

export function updateProject(projectId: string, project: Project): Promise<void> {
  return apiFetch<void>(`/projects/${projectId}`, {
    method: 'PUT',
    body: JSON.stringify(project),
  })
}

export function deleteProject(projectId: string): Promise<void> {
  return apiFetch<void>(`/projects/${projectId}`, {
    method: 'DELETE',
  })
}

export function fetchHealthCheck(): Promise<string> {
  return apiFetch<string>('/test')
}
