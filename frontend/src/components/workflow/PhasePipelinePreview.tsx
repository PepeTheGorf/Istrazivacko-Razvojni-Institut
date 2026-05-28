import { cn } from '../../lib/cn'

interface PhasePipelinePreviewProps {
  phases: Array<{ name: string; order: number }>
  className?: string
  compact?: boolean
}

export function PhasePipelinePreview({ phases, className, compact }: PhasePipelinePreviewProps) {
  const sorted = [...phases].sort((a, b) => a.order - b.order)

  if (sorted.length === 0) {
    return null
  }

  return (
    <div className={cn('flex flex-wrap items-center gap-1.5', className)}>
      {sorted.map((phase, index) => (
        <div key={`${phase.order}-${phase.name}`} className="flex items-center gap-1.5">
          <span
            className={cn(
              'rounded-md border border-hairline bg-surface-3 px-2.5 py-1.5 text-ink',
              compact ? 'text-xs' : 'text-sm',
            )}
          >
            {phase.name}
          </span>
          {index < sorted.length - 1 && (
            <span className="text-ink-tertiary select-none" aria-hidden>
              ›
            </span>
          )}
        </div>
      ))}
    </div>
  )
}
