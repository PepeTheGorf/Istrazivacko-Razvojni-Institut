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
    items: [],
  },
]

export const ADMINISTRATOR_SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    title: 'Radni Tokovi',
    items: [{ to: '/workflows', label: 'Tokovi Rada', end: true }],
  },
  {
    title: 'Resursi',
    items: [{ to: '/document-types', label: 'Tipovi Dokumenata', end: true }],
  },
]
