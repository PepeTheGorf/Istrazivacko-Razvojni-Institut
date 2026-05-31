import type { TextareaHTMLAttributes } from 'react'
import { cn } from '../../lib/cn'

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string
  name: string
}

export function TextArea({ label, className = '', ...props }: TextAreaProps) {
  return (
    <label className="grid gap-2 text-sm text-ink">
      {label ? (
        <span className="text-xs font-medium tracking-wide text-ink-subtle uppercase">
          {label}
        </span>
      ) : null}
      <textarea
        className={cn(
          'min-h-24 resize-y rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink outline-none',
          'placeholder:text-ink-tertiary focus:border-hairline-strong focus:ring-2 focus:ring-primary/30',
          className,
        )}
        {...props}
      />
    </label>
  )
}

