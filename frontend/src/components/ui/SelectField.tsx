import type { SelectHTMLAttributes } from 'react'
import { cn } from '../../lib/cn'

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  error?: string
}

export function SelectField({ label, error, id, className = '', children, ...props }: SelectFieldProps) {
  const selectId = id ?? props.name

  return (
    <label className={cn('grid gap-1', className)} htmlFor={selectId}>
      <span className="text-[13px] font-medium text-ink-muted">{label}</span>
      <select
        id={selectId}
        className={cn(
          'min-h-11 w-full appearance-none rounded-md border bg-surface-1 bg-[length:12px] bg-[position:right_12px_center] bg-no-repeat px-3 py-2 pr-9 text-base text-ink transition-colors focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50',
          "bg-[url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12' fill='none'%3E%3Cpath d='M3 4.5L6 7.5L9 4.5' stroke='%238a8f98' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E\")]",
          error ? 'border-error' : 'border-hairline',
        )}
        {...props}
      >
        {children}
      </select>
      {error && <span className="text-xs text-error">{error}</span>}
    </label>
  )
}
