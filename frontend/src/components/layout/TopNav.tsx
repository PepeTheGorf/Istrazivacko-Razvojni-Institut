import { UserMenu } from './UserMenu'
import type { Role } from '../../types/auth'

interface TopNavProps {
  role: Role
  onMenuToggle?: () => void
}

export function TopNav({ role, onMenuToggle }: TopNavProps) {
  return (
    <header className="sticky top-0 z-10 flex h-14 items-center justify-between border-b border-hairline bg-canvas px-4 md:px-6">
      <div className="flex min-w-0 items-center gap-4">
        {onMenuToggle ? (
          <button
            type="button"
            className="inline-flex h-10 w-10 cursor-pointer flex-col items-center justify-center gap-1 rounded-md border border-hairline bg-surface-1 md:hidden"
            aria-label="Otvori navigaciju"
            onClick={onMenuToggle}
          >
            <span className="block h-0.5 w-4 rounded bg-ink-muted" />
            <span className="block h-0.5 w-4 rounded bg-ink-muted" />
            <span className="block h-0.5 w-4 rounded bg-ink-muted" />
          </button>
        ) : null}
        <div className="flex min-w-0 items-center">
          <span className="truncate text-sm font-semibold tracking-tight text-ink">
            Istraživačko Razvojni Institut
          </span>
        </div>
      </div>
      <UserMenu role={role} />
    </header>
  )
}
