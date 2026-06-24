import { useState, useRef, useEffect } from 'react'
import { cn } from '../../lib/cn'

interface Option {
  id: number | string
  name: string
}

interface CreatableSelectProps {
  label: string
  options: Option[]
  placeholder?: string
  value: string | number 
  onChange: (value: { id: number | null, name: string }) => void
}

export function CreatableSelect({ label, options, placeholder, value, onChange }: CreatableSelectProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [search, setSearch] = useState('')
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) setIsOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const filteredOptions = options.filter(opt => 
    opt.name.toLowerCase().includes(search.toLowerCase())
  )

  const isNewValue = search.length > 0 && !options.some(opt => opt.name.toLowerCase() === search.toLowerCase())

  return (
    <div className="grid gap-1 relative" ref={containerRef}>
      <span className="text-[13px] font-medium text-ink-muted">{label}</span>
      
      <div className="relative">
        <input
          type="text"
          className={cn(
            "min-h-11 w-full rounded-md border border-hairline bg-surface-1 px-3 py-2 text-base text-ink focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20",
            isOpen && "border-primary-focus ring-2 ring-primary-focus/20"
          )}
          placeholder={placeholder || "Pretraži ili unesi novo..."}
          value={isOpen ? search : (options.find(o => o.id === value)?.name || search || String(value))}
          onFocus={() => {
            setIsOpen(true)
            setSearch('')
          }}
          onChange={(e) => setSearch(e.target.value)}
        />
        
        {isOpen && (
          <div className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md border border-hairline bg-surface-1 shadow-lg">
            {filteredOptions.map((opt) => (
              <button
                key={opt.id}
                className="w-full px-4 py-2 text-left hover:bg-surface-2 text-sm text-ink"
                onClick={() => {
                  onChange({ id: Number(opt.id), name: opt.name })
                  setSearch(opt.name)
                  setIsOpen(false)
                }}
              >
                {opt.name}
              </button>
            ))}

            {isNewValue && (
              <button
                className="w-full px-4 py-2 text-left hover:bg-primary/10 text-sm text-primary font-medium border-t border-hairline"
                onClick={() => {
                  onChange({ id: null, name: search })
                  setIsOpen(false)
                }}
              >
                + Dodaj novu stavku: "{search}"
              </button>
            )}

            {filteredOptions.length === 0 && !isNewValue && (
              <div className="px-4 py-2 text-sm text-ink-subtle italic">Nema rezultata... počnite da kucate za unos novog.</div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}