import { useEffect, useState } from 'react'
import { Plus, Search, X } from 'lucide-react'
import { vehicleService } from '../../services/vehicleService'
import { useDebounce } from '../../hooks/useDebounce'
import type { VehicleSummary } from '../../types/vehicle'

export function VehiclePicker({
  selectedIds,
  onAdd,
}: {
  selectedIds: string[]
  onAdd: (vehicle: VehicleSummary) => void
}) {
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounce(query, 300)
  const [results, setResults] = useState<VehicleSummary[]>([])
  const [open, setOpen] = useState(false)

  useEffect(() => {
    if (!debouncedQuery) {
      setResults([])
      return
    }
    vehicleService.search({ q: debouncedQuery, page: 1, page_size: 6 })
      .then((r) => setResults(r.items))
      .catch(() => setResults([]))
  }, [debouncedQuery])

  const canAddMore = selectedIds.length < 4

  return (
    <div className="relative">
      <div className="relative">
        <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--color-text-faint)]" />
        <input
          value={query}
          onFocus={() => setOpen(true)}
          onChange={(e) => setQuery(e.target.value)}
          disabled={!canAddMore}
          placeholder={canAddMore ? 'Ajouter un véhicule à comparer (max 4)...' : 'Maximum 4 véhicules atteint'}
          className="input-field w-full pl-9 pr-3 py-3 text-sm"
        />
        {query && (
          <button onClick={() => { setQuery(''); setResults([]) }} className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-faint)]">
            <X size={14} />
          </button>
        )}
      </div>

      {open && results.length > 0 && canAddMore && (
        <div className="absolute z-20 mt-1 w-full card-elevated py-1 max-h-72 overflow-y-auto animate-fade-in">
          {results.map((v) => {
            const alreadyAdded = selectedIds.includes(v.id)
            return (
              <button
                key={v.id}
                disabled={alreadyAdded}
                onClick={() => { onAdd(v); setQuery(''); setResults([]); setOpen(false) }}
                className="w-full flex items-center justify-between gap-2 px-3 py-2 text-left hover:bg-[var(--color-surface-3)] disabled:opacity-40"
              >
                <span className="text-sm text-[var(--color-text)]">
                  {v.make} {v.model} <span className="text-[var(--color-text-faint)]">({v.year})</span>
                </span>
                {!alreadyAdded && <Plus size={14} className="text-[var(--color-primary-400)]" />}
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
