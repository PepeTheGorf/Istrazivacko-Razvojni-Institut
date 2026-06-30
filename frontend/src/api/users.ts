import { getAuthToken } from '../auth/authStorage'
import type { TeamMember } from '../types/user'

const AUTH_BASE = import.meta.env.VITE_AUTH_API_BASE_URL ?? '/auth-api'

async function stakeholderFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getAuthToken()
  const response = await fetch(`${AUTH_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : null),
      ...init?.headers,
    },
    ...init,
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Request failed (${response.status})`)
  }

  return response.json() as Promise<T>
}

export function fetchTeamMembers(): Promise<TeamMember[]> {
  return stakeholderFetch<TeamMember[]>('/users/team-members')
}
