import type { InputHTMLAttributes } from 'react'
import { cn } from '../../lib/cn'

interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
}

export function TextInput({ label, error, id, className = '', ...props }: TextInputProps) {
  const inputId = id ?? props.name

  return (
    <label className={cn('grid gap-1', className)} htmlFor={inputId}>
      <span className="text-[13px] font-medium text-ink-muted">{label}</span>
      <input
        id={inputId}
        className={cn(
          'min-h-11 w-full rounded-md border bg-surface-1 px-3 py-2 text-base text-ink transition-colors placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50',
          error ? 'border-error' : 'border-hairline',
        )}
        {...props}
      />
      {error && <span className="text-xs text-error">{error}</span>}
    </label>
  )
}
