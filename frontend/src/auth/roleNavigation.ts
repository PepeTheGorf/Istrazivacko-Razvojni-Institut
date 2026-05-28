import type { Role } from '../types/auth'

export function getHomePathForRole(role: Role): string {
  switch (role) {
    case 'ADMINISTRATOR':
      return '/workflows'
    case 'MANAGER':
    case 'TEAM_MEMBER':
      return '/projects'
    default:
      return '/login'
  }
}
