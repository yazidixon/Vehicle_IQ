import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { CarFront, ChevronLeft, ChevronRight, GitCompare } from 'lucide-react'
import { SearchFiltersPanel } from '../components/search/SearchFilters'
import { VehicleCard } from '../components/vehicle/VehicleCard'
import { SkeletonGrid } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { vehicleService } from '../services/vehicleService'
import { extractApiError } from '../services/api'
import { useDebounce } from '../hooks/useDebounce'
import type { PaginatedResponse, VehicleSearchFilters, VehicleSummary } from '../types/vehicle'
import { useNavigate } from 'react-router-dom'

const emptyFilters: VehicleSearchFilters = { page: 1, page_size: 12 }

export function SearchPage() {
  const [searchParams] = useSearchParams()
  const [filters, setFilters] = useState<VehicleSearchFilters>({
    ...emptyFilters,
    q: searchParams.get('q') || undefined,
  })
  const debouncedFilters = useDebounce(filters, 350)

  const [result, setResult] = useState<PaginatedResponse<VehicleSummary> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<string[]>([])
  const navigate = useNavigate()

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    vehicleService.search(debouncedFilters)
      .then((data) => { if (!cancelled) setResult(data) })
      .catch((err) => { if (!cancelled) setError(extractApiError(err).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [debouncedFilters])

  function toggleCompare(id: string) {
    setSelected((prev) => (prev.includes(id) ? prev.filter((v) => v !== id) : prev.length < 4 ? [...prev, id] : prev))
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      <div className="mb-6">
        <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Rechercher un véhicule</h1>
        <p className="text-sm text-[var(--color-text-muted)] mt-1">Combinez les filtres pour affiner vos résultats.</p>
      </div>

      <div className="mb-6">
        <input
          value={filters.q || ''}
          onChange={(e) => setFilters({ ...filters, q: e.target.value || undefined, page: 1 })}
          placeholder="Recherche libre : marque, modèle, constructeur..."
          className="input-field w-full px-4 py-3 text-sm"
        />
      </div>

      <div className="mb-6">
        <SearchFiltersPanel filters={filters} onChange={setFilters} onReset={() => setFilters(emptyFilters)} />
      </div>

      {selected.length > 0 && (
        <div className="mb-6 card-elevated p-4 flex items-center justify-between flex-wrap gap-3 animate-fade-in">
          <p className="text-sm text-[var(--color-text)]">
            <strong>{selected.length}</strong> véhicule(s) sélectionné(s) pour comparaison
          </p>
          <div className="flex gap-2">
            <button onClick={() => setSelected([])} className="btn-secondary px-3 py-1.5 text-xs">Vider</button>
            <button
              disabled={selected.length < 2}
              onClick={() => navigate(`/compare?ids=${selected.join(',')}`)}
              className="btn-primary px-4 py-1.5 text-xs flex items-center gap-1.5"
            >
              <GitCompare size={13} /> Comparer
            </button>
          </div>
        </div>
      )}

      {loading && <SkeletonGrid count={9} />}

      {!loading && error && (
        <ErrorState message={error} onRetry={() => setFilters({ ...filters })} />
      )}

      {!loading && !error && result && result.items.length === 0 && (
        <EmptyState
          icon={<CarFront size={32} />}
          title="Aucun véhicule trouvé"
          description="Essayez d'élargir vos critères de recherche ou réinitialisez les filtres."
          action={<button onClick={() => setFilters(emptyFilters)} className="btn-secondary px-4 py-2 text-sm">Réinitialiser les filtres</button>}
        />
      )}

      {!loading && !error && result && result.items.length > 0 && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {result.items.map((v) => (
              <VehicleCard
                key={v.id}
                vehicle={v}
                onToggleCompare={toggleCompare}
                isSelected={selected.includes(v.id)}
                compareDisabled={selected.length >= 4}
              />
            ))}
          </div>

          <div className="flex items-center justify-between mt-8">
            <p className="text-xs text-[var(--color-text-muted)]">
              {result.total} résultat(s) — page {result.page} / {result.total_pages}
            </p>
            <div className="flex gap-2">
              <button
                disabled={filters.page === 1}
                onClick={() => setFilters({ ...filters, page: (filters.page || 1) - 1 })}
                className="btn-secondary px-3 py-2 text-sm disabled:opacity-40"
              >
                <ChevronLeft size={15} />
              </button>
              <button
                disabled={(filters.page || 1) >= result.total_pages}
                onClick={() => setFilters({ ...filters, page: (filters.page || 1) + 1 })}
                className="btn-secondary px-3 py-2 text-sm disabled:opacity-40"
              >
                <ChevronRight size={15} />
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
