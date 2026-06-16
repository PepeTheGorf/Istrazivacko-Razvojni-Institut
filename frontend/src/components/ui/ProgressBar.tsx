interface ProgressBarProps {
  progress: number
  label?: string
}

export function ProgressBar({ progress, label }: ProgressBarProps) {
  return (
    <div className="w-full">
      {label && (
        <div className="mb-1 flex justify-between text-xs font-medium text-ink-subtle">
          <span>{label}</span>
          <span>{progress}%</span>
        </div>
      )}
      <div className="h-2 w-full rounded-full bg-surface-3 overflow-hidden">
        <div 
          className="h-full bg-primary transition-all duration-500 ease-out"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  )
}