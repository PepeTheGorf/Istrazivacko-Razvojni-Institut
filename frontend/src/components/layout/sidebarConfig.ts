export interface SidebarNavItem {
  to?: string
  label: string
  end?: boolean
  disabled?: boolean
}

export interface SidebarSection {
  title: string
  items: SidebarNavItem[]
}

export const MANAGER_SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    title: 'Radni Tokovi',
    items: [{ to: '/projects', label: 'Projekti', end: true }],
  },
  {
    title: 'Resursi',
    items: [{ to: '/pristup', label: 'Upravljanje pristupom', end: true }],
  },
]

export const TEAM_MEMBER_SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    title: 'Zadaci',
    items: [{ to: '/my-tasks', label: 'Moji zadaci' }],
  },
  {
    title: 'Dokumenti',
    items: [{ to: '/documents', label: 'Dokumenti', end: true }],
  },
]

export const ADMINISTRATOR_SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    title: 'Radni Tokovi',
    items: [{ to: '/workflows', label: 'Tokovi Rada', end: true }],
  },
  {
    title: 'Resursi',
    items: [
      { to: '/document-types', label: 'Tipovi Dokumenata', end: true },
      { to: '/pristup', label: 'Upravljanje pristupom', end: true },
    ],
  },
]
