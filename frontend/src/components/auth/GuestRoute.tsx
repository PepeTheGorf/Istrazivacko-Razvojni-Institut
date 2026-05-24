import { useEffect } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'

export function GuestRoute() {
  const { isAuthenticated, user, logout } = useAuth()

  useEffect(() => {
    if (isAuthenticated && user?.role !== 'MANAGER') {
      logout()
    }
  }, [isAuthenticated, user, logout])

  if (isAuthenticated && user?.role === 'MANAGER') {
    return <Navigate to="/projects" replace />
  }

  return <Outlet />
}
