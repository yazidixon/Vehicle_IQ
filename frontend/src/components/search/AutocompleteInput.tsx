import { useEffect, useRef, useState } from 'react'
import { ChevronDown, X } from 'lucide-react'

export function AutocompleteInput({
  label,
  value,
  options,
  onChange,
  placeholder,
  disabled,
}: {
  label: string
  value: string
  options: string[]
  onChange: (value: string) => void
  placeholder?: string
  disabled?: boolean
}) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState(value)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => setQuery(value), [value])

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const filtered = options.filter((o) => o.toLowerCase().includes(query.toLowerCase())).slice(0, 40)

  return (
    <div ref={containerRef} className="relative">
      <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">{label}</label>
      <div className="relative">
        <input
          className="input-field w-full px-3 py-2 pr-16 text-sm"
          placeholder={placeholder}
          value={query}
          disabled={disabled}
          onFocus={() => setOpen(true)}
          onChange={(e) => {
            setQuery(e.target.value)
            onChange(e.target.value)
            setOpen(true)
          }}
        />
        <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1">
          {value && (
            <button
              type="button"
              onClick={() => {
                setQuery('')
                onChange('')
              }}
              className="text-[var(--color-text-faint)] hover:text-[var(--color-text)]"
            >
              <X size={14} />
            </button>
          )}
          <ChevronDown size={14} className="text-[var(--color-text-faint)]" />
        </div>
      </div>
      {open && filtered.length > 0 && (
        <div className="absolute z-20 mt-1 w-full max-h-56 overflow-y-auto card-elevated py-1 animate-fade-in">
          {filtered.map((option) => (
            <button
              key={option}
              type="button"
              className="w-full text-left px-3 py-1.5 text-sm text-[var(--color-text)] hover:bg-[var(--color-surface-3)]"
              onClick={() => {
                onChange(option)
                setQuery(option)
                setOpen(false)
              }}
            >
              {option}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
