import type { AuthResponse, AuthUser, StoredAuth } from '../types/auth'

const STORAGE_KEY = 'iri_auth'

function toAuthUser(response: AuthResponse): AuthUser {
  return {
    id: response.id,
    name: response.name,
    surname: response.surname,
    email: response.email,
    role: response.role,
    createdAt: response.createdAt,
  }
}

export function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1])) as { exp?: number }
    if (!payload.exp) {
      return true
    }
    return payload.exp * 1000 <= Date.now()
  } catch {
    return true
  }
}

export function getStoredAuth(): StoredAuth | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw) as StoredAuth
    if (!parsed.token || !parsed.user || isTokenExpired(parsed.token)) {
      localStorage.removeItem(STORAGE_KEY)
      return null
    }
    return parsed
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function saveAuth(response: AuthResponse): StoredAuth {
  const stored: StoredAuth = {
    token: response.token,
    user: toAuthUser(response),
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(stored))
  return stored
}

export function clearAuth(): void {
  localStorage.removeItem(STORAGE_KEY)
}

export function getAuthToken(): string | null {
  return getStoredAuth()?.token ?? null
}
