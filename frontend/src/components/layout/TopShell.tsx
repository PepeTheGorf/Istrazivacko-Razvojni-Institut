import type { ReactNode } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { TopNav } from './TopNav'

interface TopShellProps {
  children: ReactNode
}

export function TopShell({ children }: TopShellProps) {
  const { user } = useAuth()

  if (!user) {
    return null
  }

  return (
    <div className="min-h-screen bg-canvas text-ink">
      <TopNav role={user.role} />
      <main className="min-w-0 px-4 py-4 md:px-6 md:py-6">{children}</main>
    </div>
  )
}