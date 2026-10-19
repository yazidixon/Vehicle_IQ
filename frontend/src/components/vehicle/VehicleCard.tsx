import { Link } from 'react-router-dom'
import { Car, Fuel, Gauge, GitCompare, Settings2 } from 'lucide-react'
import type { VehicleSummary } from '../../types/vehicle'
import { DataSourceBadge } from '../ui/Badge'
import { formatValue } from '../../utils/formatters'

export function VehicleCard({
  vehicle,
  onToggleCompare,
  isSelected,
  compareDisabled,
}: {
  vehicle: VehicleSummary
  onToggleCompare?: (id: string) => void
  isSelected?: boolean
  compareDisabled?: boolean
}) {
  return (
    <div className="card-elevated card-hover flex flex-col overflow-hidden animate-fade-in">
      <div className="h-36 bg-grid bg-[var(--color-surface-2)] flex items-center justify-center border-b border-[var(--color-border)]">
        {vehicle.image_url ? (
          <img src={vehicle.image_url} alt={`${vehicle.make} ${vehicle.model}`} className="h-full w-full object-cover" />
        ) : (
          <Car size={40} className="text-[var(--color-text-faint)]" />
        )}
      </div>
      <div className="p-4 flex-1 flex flex-col gap-3">
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-xs text-[var(--color-text-muted)]">{vehicle.manufacturer}</p>
            <h3 className="font-display font-semibold text-[var(--color-text)] leading-tight">
              {vehicle.make} {vehicle.model}
            </h3>
          </div>
          <DataSourceBadge isDemoData={vehicle.is_demo_data} />
        </div>

        <div className="grid grid-cols-2 gap-2 text-xs text-[var(--color-text-muted)]">
          <span className="flex items-center gap-1.5"><Gauge size={13} /> {vehicle.year}</span>
          <span className="flex items-center gap-1.5"><Fuel size={13} /> {formatValue(vehicle.fuel_type)}</span>
          <span className="flex items-center gap-1.5"><Settings2 size={13} /> {formatValue(vehicle.horsepower_hp, 'ch')}</span>
          <span className="flex items-center gap-1.5 truncate">{formatValue(vehicle.transmission)}</span>
        </div>

        <div className="mt-auto flex gap-2 pt-2">
          <Link to={`/vehicles/${vehicle.id}`} className="btn-primary flex-1 text-center text-sm py-2">
            Voir la fiche
          </Link>
          {onToggleCompare && (
            <button
              onClick={() => onToggleCompare(vehicle.id)}
              disabled={compareDisabled && !isSelected}
              title="Ajouter à la comparaison"
              className={`px-3 rounded-xl border text-sm flex items-center justify-center transition-colors ${
                isSelected
                  ? 'bg-[var(--color-primary-500)]/20 border-[var(--color-primary-500)] text-[var(--color-primary-400)]'
                  : 'btn-secondary'
              }`}
            >
              <GitCompare size={16} />
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
