import type { ButtonHTMLAttributes } from 'react'
import { cn } from '../../lib/cn'
import { BUTTON_ICON_SIZE, BUTTON_ICONS, type ButtonIconName } from './buttonIcons'

type ButtonVariant = 'primary' | 'secondary' | 'tertiary' | 'delete'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  fullWidth?: boolean
  icon?: ButtonIconName
}

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    'bg-primary text-on-primary hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-focus/50',
  secondary:
    'border border-hairline bg-surface-1 text-ink hover:border-hairline-strong hover:bg-surface-2',
  tertiary: 'bg-transparent text-ink-muted hover:bg-surface-1 hover:text-ink',
  delete: 'bg-transparent text-error hover:bg-error/10 hover:text-error',
}

export function Button({
  variant = 'primary',
  fullWidth = false,
  icon,
  className = '',
  type = 'button',
  children,
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
    >
      {icon && (
        <img
          src={BUTTON_ICONS[icon]}
          alt=""
          width={BUTTON_ICON_SIZE}
          height={BUTTON_ICON_SIZE}
          aria-hidden
        />
      )}
      {children}
    </button>
  )
}
