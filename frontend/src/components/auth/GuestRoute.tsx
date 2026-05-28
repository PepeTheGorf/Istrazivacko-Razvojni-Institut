import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { getHomePathForRole } from '../../auth/roleNavigation'

export function GuestRoute() {
  const { isAuthenticated, user } = useAuth()

  if (isAuthenticated && user) {
    return <Navigate to={getHomePathForRole(user.role)} replace />
  }

  return <Outlet />
}
