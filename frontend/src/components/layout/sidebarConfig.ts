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
    title: 'Pametni Šabloni', 
    items: [
      { to: '/smart-templates', label: 'Lista Šablona', end: true },
      { to: '/smart-templates/new', label: 'Novi Šablon' },
    ],
  },
  {
    title: 'Resursi',
    items: [],
  },
]

export const TEAM_MEMBER_SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    title: 'Zadaci',
    items: [{ to: '/my-tasks', label: 'Moji zadaci' }],
  },
  {
    title: 'Dokumenti',
    items: [{ to: '/documents', label: 'Lista Dokumenata', end: true }],
  },
  {
    title: 'AI Dokumentacija', 
    items: [
      { to: '/smart-docs', label: 'Nova AI Dokumentacija' },
    ],
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
      { to: '/document-types', label: 'Tipovi Dokumenata', end: true }
    ],
  },
]