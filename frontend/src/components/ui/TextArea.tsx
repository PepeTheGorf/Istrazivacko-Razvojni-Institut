import type { TextareaHTMLAttributes } from 'react'
import { cn } from '../../lib/cn'

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string
  name: string
}

export function TextArea({ label, className = '', ...props }: TextAreaProps) {
  return (
    <>
      <label className="grid gap-2 text-sm text-ink">
        {label ? (
          <span className="text-xs font-medium tracking-wide text-ink-subtle uppercase leading-none">
            {label}
          </span>
        ) : null}
        <textarea
          className={cn(
            'min-h-24 resize-y rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink outline-none transition-all',
            'placeholder:text-ink-tertiary focus:border-primary/50 focus:ring-2 focus:ring-primary/20',
            'custom-scrollbar', 
            className,
          )}
          {...props}
        />
      </label>

      <style>{`
        .custom-scrollbar {
          scrollbar-width: thin;
          scrollbar-color: rgba(138, 143, 152, 0.3) transparent;
        }
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background-color: rgba(138, 143, 152, 0.3);
          border-radius: 20px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background-color: rgba(138, 143, 152, 0.5);
        }
      `}</style>
    </>
  )
}