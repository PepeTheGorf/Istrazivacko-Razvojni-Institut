import { NavLink } from 'react-router-dom'
import { cn } from '../../lib/cn'
import {
  ADMINISTRATOR_SIDEBAR_SECTIONS,
  MANAGER_SIDEBAR_SECTIONS,
} from './sidebarConfig'
import { useAuth } from '../../auth/AuthContext'

interface SidebarProps {
  open: boolean
  onClose: () => void
}

function navLinkClass(isActive: boolean) {
  return cn(
    'relative block w-full py-2.5 text-sm transition-all duration-150 ease-out',
    'border-l-[5px]',
    isActive
      ? 'border-l-primary bg-primary/20 pl-[calc(1rem-3px)] font-medium text-ink'
      : 'border-l-transparent pl-4 text-ink-muted hover:bg-surface-2/90 hover:text-ink',
  )
}

export function Sidebar({ open, onClose }: SidebarProps) {
  const { user } = useAuth()
  const sections = user?.role === 'ADMINISTRATOR'
    ? ADMINISTRATOR_SIDEBAR_SECTIONS
    : MANAGER_SIDEBAR_SECTIONS

  return (
    <>
      {open && (
        <button
          type="button"
          className="fixed inset-0 top-14 z-[15] border-none bg-black/55 md:hidden"
          aria-label="Zatvori meni"
          onClick={onClose}
        />
      )}
      <aside
        className={cn(
          'w-60 shrink-0 border-r border-hairline bg-canvas',
          'md:static md:translate-x-0',
          'fixed top-14 bottom-0 left-0 z-20 overflow-y-auto transition-transform duration-200 md:relative md:top-auto',
          open ? 'translate-x-0' : '-translate-x-full md:translate-x-0',
        )}
      >
        <nav className="flex flex-col">
          {sections.map((section, index) => (
            <section
              key={section.title}
              className={cn('flex flex-col', index > 0 && 'border-t border-hairline')}
            >
              <h2 className="m-0 px-4 pt-4 pb-2 text-[11px] font-semibold tracking-wider text-ink-subtle uppercase">
                {section.title}
              </h2>

              {section.items.length > 0 ? (
                <ul className="m-0 flex list-none flex-col p-0">
                  {section.items.map((item) => (
                    <li key={item.to ?? item.label}>
                      {item.disabled || !item.to ? (
                        <span
                          className={cn(
                            navLinkClass(false),
                            'cursor-not-allowed opacity-50',
                          )}
                          aria-disabled="true"
                        >
                          {item.label}
                        </span>
                      ) : (
                        <NavLink
                          to={item.to}
                          end={item.end}
                          className={({ isActive }) => navLinkClass(isActive)}
                          onClick={onClose}
                        >
                          {item.label}
                        </NavLink>
                      )}
                    </li>
                  ))}
                </ul>
              ) : null}
            </section>
          ))}
        </nav>
      </aside>
    </>
  )
}
