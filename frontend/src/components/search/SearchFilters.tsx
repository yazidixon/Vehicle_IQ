import { useEffect, useState } from 'react'
import { RotateCcw, Search as SearchIcon } from 'lucide-react'
import { AutocompleteInput } from './AutocompleteInput'
import { vehicleService } from '../../services/vehicleService'
import type { VehicleSearchFilters } from '../../types/vehicle'

const FUEL_TYPES = ['Essence', 'Diesel', 'Hybride', 'Hybride rechargeable', 'Électrique', 'Hybride léger (MHEV)', 'Essence (MHEV)']

export function SearchFiltersPanel({
  filters,
  onChange,
  onReset,
}: {
  filters: VehicleSearchFilters
  onChange: (filters: VehicleSearchFilters) => void
  onReset: () => void
}) {
  const [makes, setMakes] = useState<string[]>([])
  const [models, setModels] = useState<string[]>([])

  useEffect(() => {
    vehicleService.listMakes().then(setMakes).catch(() => setMakes([]))
  }, [])

  useEffect(() => {
    if (filters.make) {
      vehicleService.listModels(filters.make).then(setModels).catch(() => setModels([]))
    } else {
      setModels([])
    }
  }, [filters.make])

  return (
    <div className="card-elevated p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2">
          <SearchIcon size={16} /> Filtres
        </h3>
        <button onClick={onReset} className="text-xs text-[var(--color-text-muted)] hover:text-[var(--color-text)] flex items-center gap-1">
          <RotateCcw size={12} /> Réinitialiser
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <AutocompleteInput
          label="Marque"
          value={filters.make || ''}
          options={makes}
          placeholder="Toutes les marques"
          onChange={(make) => onChange({ ...filters, make: make || undefined, model: undefined, page: 1 })}
        />
        <AutocompleteInput
          label="Modèle"
          value={filters.model || ''}
          options={models}
          placeholder={filters.make ? 'Tous les modèles' : 'Choisir une marque d\'abord'}
          disabled={!filters.make}
          onChange={(model) => onChange({ ...filters, model: model || undefined, page: 1 })}
        />
        <div>
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Année</label>
          <input
            type="number" min={1990} max={2030}
            value={filters.year || ''}
            onChange={(e) => onChange({ ...filters, year: e.target.value ? Number(e.target.value) : undefined, page: 1 })}
            className="input-field w-full px-3 py-2 text-sm" placeholder="Ex : 2023"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Carburant</label>
          <select
            value={filters.fuel_type || ''}
            onChange={(e) => onChange({ ...filters, fuel_type: e.target.value || undefined, page: 1 })}
            className="input-field w-full px-3 py-2 text-sm"
          >
            <option value="">Tous les carburants</option>
            {FUEL_TYPES.map((f) => <option key={f} value={f}>{f}</option>)}
          </select>
        </div>
      </div>
    </div>
  )
}
