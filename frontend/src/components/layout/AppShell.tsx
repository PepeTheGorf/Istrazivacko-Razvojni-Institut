import { useState, type ReactNode } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { Sidebar } from './Sidebar'
import { TopNav } from './TopNav'

interface AppShellProps {
  children: ReactNode
  hideSidebar?: boolean
}

export function AppShell({ children, hideSidebar = false }: AppShellProps) {
  const { user } = useAuth()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  if (!user) {
    return null
  }

  return (
    <div className="min-h-screen bg-canvas text-ink">
      <TopNav role={user.role} onMenuToggle={() => setSidebarOpen(true)} />
      <div className="flex min-h-[calc(100vh-56px)]">
        {!hideSidebar && (
        <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
        )}
        <main className="min-w-0 flex-1 p-4 md:p-6">{children}</main>
      </div>
    </div>
  )
}
