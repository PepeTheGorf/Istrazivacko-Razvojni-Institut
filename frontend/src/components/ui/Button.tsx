import type { ButtonHTMLAttributes } from 'react'
import { cn } from '../../lib/cn'

type ButtonVariant = 'primary' | 'secondary' | 'tertiary'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  fullWidth?: boolean
}

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    'bg-primary text-on-primary hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-focus/50',
  secondary:
    'border border-hairline bg-surface-1 text-ink hover:border-hairline-strong hover:bg-surface-2',
  tertiary: 'bg-transparent text-ink-muted hover:bg-surface-1 hover:text-ink',
}

export function Button({
  variant = 'primary',
  fullWidth = false,
  className = '',
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        'inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-md px-3.5 py-2 text-sm font-medium leading-tight transition-colors disabled:cursor-not-allowed disabled:opacity-55',
        variantClasses[variant],
        fullWidth && 'w-full',
        className,
      )}
      {...props}
    />
  )
}
