import {
  useCallback,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { login as loginRequest, register as registerRequest } from '../api/auth'
import { clearAuth, getStoredAuth, saveAuth } from './authStorage'
import type { LoginRequest, RegisterRequest } from '../types/auth'
import { AuthContext, type AuthContextValue } from './AuthProviderContext'

export { useAuth } from './useAuth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState(() => getStoredAuth())

  const login = useCallback(async (request: LoginRequest) => {
    const response = await loginRequest(request)
    setAuth(saveAuth(response))
  }, [])

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await registerRequest(request)
    setAuth(saveAuth(response))
  }, [])

  const logout = useCallback(() => {
    clearAuth()
    setAuth(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user: auth?.user ?? null,
      token: auth?.token ?? null,
      isAuthenticated: auth !== null,
      login,
      register,
      logout,
    }),
    [auth, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
