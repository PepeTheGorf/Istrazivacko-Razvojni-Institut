export type Role = 'ADMINISTRATOR' | 'MANAGER' | 'TEAM_MEMBER'

export type RegisterableRole = 'MANAGER' | 'TEAM_MEMBER'

export interface AuthUser {
  id: number
  name: string
  surname: string
  email: string
  role: Role
  createdAt: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  id: number
  name: string
  surname: string
  email: string
  role: Role
  createdAt: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  surname: string
  email: string
  password: string
  role: RegisterableRole
}

export interface StoredAuth {
  token: string
  user: AuthUser
}
