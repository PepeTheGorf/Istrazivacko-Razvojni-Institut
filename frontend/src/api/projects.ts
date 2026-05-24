import { apiFetch } from './client'
import type { Project } from '../types/project'

export function fetchProjects(): Promise<Project[]> {
  return apiFetch<Project[]>('/projects/all')
}

export function createProject(project: Project): Promise<void> {
  return apiFetch<void>('/projects', {
    method: 'POST',
    body: JSON.stringify(project),
  })
}

export function fetchHealthCheck(): Promise<string> {
  return apiFetch<string>('/test')
}
