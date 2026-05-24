export interface SidebarNavItem {
  to: string
  label: string
  end?: boolean
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
