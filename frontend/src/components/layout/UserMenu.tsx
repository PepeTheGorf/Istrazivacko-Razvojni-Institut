import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import type { Role } from '../../types/auth'

const ROLE_LABELS: Record<Role, string> = {
  ADMINISTRATOR: 'ADMINISTRATOR',
  MANAGER: 'MENADŽER',
  TEAM_MEMBER: 'ČLAN TIMA',
}

interface UserMenuProps {
  role: Role
}

export function UserMenu({ role }: UserMenuProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  const initials = user
    ? `${user.name.charAt(0)}${user.surname.charAt(0)}`.toUpperCase()
    : '?'

  return (
    <div className="relative flex items-center gap-3" ref={menuRef}>
      <span className="hidden text-xs font-medium tracking-wide text-ink-subtle uppercase sm:inline">
        {ROLE_LABELS[role]}
      </span>
      <button
        type="button"
        className="cursor-pointer border-none bg-transparent p-0"
        aria-label="Korisnički meni"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
      >
        <span className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-hairline bg-surface-2 text-[13px] font-semibold text-ink">
          {initials}
        </span>
      </button>

      {open && (
        <div className="absolute top-[calc(100%+8px)] right-0 z-30 min-w-[220px] rounded-md border border-hairline-strong bg-surface-2 p-2 shadow-[0_12px_32px_rgba(0,0,0,0.45)]">
          <div className="grid gap-0.5 border-b border-hairline px-3 py-3">
            <strong className="text-sm text-ink">
              {user?.name} {user?.surname}
            </strong>
            <span className="text-xs text-ink-subtle">{user?.email}</span>
          </div>
          <button
            type="button"
            className="mt-2 w-full cursor-pointer rounded-sm border-none bg-transparent px-3 py-2.5 text-left text-sm text-ink hover:bg-surface-3"
            onClick={handleLogout}
          >
            Odjavi se
          </button>
        </div>
      )}
    </div>
  )
}
