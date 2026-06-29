interface ChevronIconProps {
  expanded: boolean
}

export function ChevronIcon({ expanded }: ChevronIconProps) {
  return (
    <svg
      viewBox="0 0 20 20"
      aria-hidden
      className={`h-4 w-4 shrink-0 text-ink-subtle transition-transform duration-200 ${expanded ? 'rotate-90' : 'rotate-0'}`}
    >
      <path
        d="M7 5l6 5-6 5"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
