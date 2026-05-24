import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth'

const AUTH_BASE = import.meta.env.VITE_AUTH_API_BASE_URL ?? '/auth-api'

async function authFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${AUTH_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
    ...init,
  })

  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try {
      const body = (await response.json()) as { error?: string; errors?: Record<string, string> }
      if (body.error) {
        message = body.error
      } else if (body.errors) {
        message = Object.values(body.errors).join(', ')
      }
    } catch {
      const text = await response.text()
      if (text) {
        message = text
      }
    }
    throw new Error(message)
  }

  return response.json() as Promise<T>
}

export function login(request: LoginRequest): Promise<AuthResponse> {
  return authFetch<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function register(request: RegisterRequest): Promise<AuthResponse> {
  return authFetch<AuthResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
