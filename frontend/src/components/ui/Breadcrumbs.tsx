import { Link } from 'react-router-dom'

interface BreadcrumbItem {
  label: string
  to?: string
}

export function Breadcrumbs({ items }: { items: BreadcrumbItem[] }) {
  return (
    <nav className="mb-6 flex items-center gap-2 text-xs font-medium uppercase tracking-wider">
      {items.map((item, index) => (
        <div key={index} className="flex items-center gap-2">
          {item.to ? (
            <Link to={item.to} className="text-ink-subtle hover:text-primary transition-colors">
              {item.label}
            </Link>
          ) : (
            <span className="text-ink">{item.label}</span>
          )}
          {index < items.length - 1 && <span className="text-hairline-strong">/</span>}
        </div>
      ))}
    </nav>
  )
}