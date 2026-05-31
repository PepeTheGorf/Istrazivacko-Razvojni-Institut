import type { Role } from '../types/auth'

export function getHomePathForRole(role: Role): string {
  switch (role) {
    case 'ADMINISTRATOR':
      return '/workflows'
    case 'MANAGER':
      return '/projects'
    case 'TEAM_MEMBER':
      return '/my-tasks'
    default:
      return '/login'
  }
}
