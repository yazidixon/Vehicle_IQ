import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  ArrowLeft, Car, Cog, Fuel, GitCompare, Info, Leaf, Ruler, ShieldCheck, Zap, Database,
} from 'lucide-react'
import { vehicleService } from '../services/vehicleService'
import { extractApiError } from '../services/api'
import { DataSourceBadge } from '../components/ui/Badge'
import { SpecRow } from '../components/vehicle/SpecRow'
import { ErrorState } from '../components/ui/ErrorState'
import { Skeleton } from '../components/ui/Skeleton'
import { formatDate, formatPriceInMad } from '../utils/formatters'
import type { Vehicle, VehicleSourceData } from '../types/vehicle'

export function VehicleDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [vehicle, setVehicle] = useState<Vehicle | null>(null)
  const [sources, setSources] = useState<VehicleSourceData[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showSources, setShowSources] = useState(false)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError(null)
    Promise.all([vehicleService.getById(id), vehicleService.getSources(id)])
      .then(([v, s]) => { setVehicle(v); setSources(s) })
      .catch((err) => setError(extractApiError(err).message))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-10 space-y-4">
        <Skeleton className="h-8 w-1/3" />
        <Skeleton className="h-48 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (error || !vehicle) {
    return (
      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
        <ErrorState message={error || 'Véhicule introuvable.'} onRetry={() => window.location.reload()} />
      </div>
    )
  }

  const spec = vehicle.specification

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
      <button onClick={() => navigate(-1)} className="text-sm text-[var(--color-text-muted)] hover:text-[var(--color-text)] flex items-center gap-1.5 mb-5">
        <ArrowLeft size={14} /> Retour
      </button>

      <div className="card-elevated overflow-hidden mb-6">
        <div className="h-56 bg-grid bg-[var(--color-surface-2)] flex items-center justify-center border-b border-[var(--color-border)]">
          {vehicle.image_url ? (
            <img src={vehicle.image_url} alt={vehicle.model} className="h-full w-full object-cover" />
          ) : (
            <Car size={56} className="text-[var(--color-text-faint)]" />
          )}
        </div>
        <div className="p-6 flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm text-[var(--color-text-muted)]">{vehicle.manufacturer} {vehicle.generation && `— ${vehicle.generation}`}</p>
            <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">{vehicle.make} {vehicle.model}</h1>
            <div className="flex flex-wrap items-center gap-2 mt-2">
              <span className="badge badge-neutral">{vehicle.year}</span>
              {vehicle.trim && <span className="badge badge-neutral">{vehicle.trim}</span>}
              {vehicle.fuel_type && <span className="badge badge-primary">{vehicle.fuel_type}</span>}
              <DataSourceBadge isDemoData={vehicle.is_demo_data} />
            </div>
          </div>
          <div className="flex gap-2">
            <button onClick={() => navigate(`/compare?ids=${vehicle.id}`)} className="btn-secondary px-4 py-2 text-sm flex items-center gap-2">
              <GitCompare size={15} /> Comparer
            </button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <section className="card-elevated p-5">
          <h2 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2 mb-1">
            <Info size={16} /> Identification
          </h2>
          <SpecRow label="Constructeur" value={vehicle.manufacturer} />
          <SpecRow label="Marque" value={vehicle.make} />
          <SpecRow label="Modèle" value={vehicle.model} />
          <SpecRow label="Génération" value={vehicle.generation} />
          <SpecRow label="Année de production" value={vehicle.year} />
          <SpecRow label="Type de carrosserie" value={vehicle.body_type} />
        </section>

        <section className="card-elevated p-5">
          <h2 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2 mb-1">
            <Cog size={16} /> Motorisation
          </h2>
          <SpecRow label="Type de moteur" value={spec?.engine_type} />
          <SpecRow label="Cylindrée" value={spec?.engine_displacement_cc} unit="cm³" />
          <SpecRow label="Puissance" value={spec?.horsepower_hp} unit="ch" />
          <SpecRow label="Couple" value={spec?.torque_nm} unit="Nm" />
          <SpecRow label="Boîte de vitesses" value={spec?.transmission} />
          <SpecRow label="Transmission" value={spec?.drivetrain} />
        </section>

        <section className="card-elevated p-5">
          <h2 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2 mb-1">
            <Zap size={16} /> Performances
          </h2>
          <SpecRow label="Puissance" value={spec?.horsepower_hp} unit="ch" />
          <SpecRow label="Couple" value={spec?.torque_nm} unit="Nm" />
          <SpecRow label="0 à 100 km/h" value={spec?.acceleration_0_100_s} unit="s" />
          <SpecRow label="Vitesse maximale" value={spec?.top_speed_kmh} unit="km/h" />
        </section>

        <section className="card-elevated p-5">
          <h2 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2 mb-1">
            <Ruler size={16} /> Dimensions
          </h2>
          <SpecRow label="Longueur" value={spec?.length_mm} unit="mm" />
          <SpecRow label="Largeur" value={spec?.width_mm} unit="mm" />
          <SpecRow label="Hauteur" value={spec?.height_mm} unit="mm" />
          <SpecRow label="Empattement" value={spec?.wheelbase_mm} unit="mm" />
          <SpecRow label="Poids" value={spec?.weight_kg} unit="kg" />
          <SpecRow label="Portes" value={spec?.doors} />
          <SpecRow label="Places" value={spec?.seats} />
        </section>

        <section className="card-elevated p-5">
          <h2 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2 mb-1">
            <Fuel size={16} /> Consommation
          </h2>
          <SpecRow label="Urbaine" value={spec?.fuel_consumption_urban_l100km} unit="L/100km" />
          <SpecRow label="Extra-urbaine" value={spec?.fuel_consumption_extra_urban_l100km} unit="L/100km" />
          <SpecRow label="Mixte" value={spec?.fuel_consumption_mixed_l100km} unit="L/100km" />
          <SpecRow label="Prix indicatif" value={spec?.price_msrp != null ? formatPriceInMad(spec.price_msrp) : null} />
        </section>

        <section className="card-elevated p-5">
          <h2 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2 mb-1">
            <Leaf size={16} /> Environnement
          </h2>
          <SpecRow label="Émissions CO₂" value={spec?.co2_emissions_gkm} unit="g/km" />
          <SpecRow label="Norme Euro" value={spec?.euro_norm} />
          <SpecRow label="Classe énergétique" value={spec?.energy_class} />
        </section>

        <section className="card-elevated p-5 lg:col-span-2">
          <h2 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-2 mb-3">
            <ShieldCheck size={16} /> Équipements &amp; sécurité
          </h2>
          {spec?.equipment && spec.equipment.length > 0 ? (
            <div className="flex flex-wrap gap-2 mb-3">
              {spec.equipment.map((eq) => <span key={eq} className="badge badge-neutral">{eq}</span>)}
            </div>
          ) : (
            <p className="text-sm text-[var(--color-text-faint)] italic mb-3">Data not available</p>
          )}
          <p className="text-sm text-[var(--color-text-muted)]">{spec?.safety_notes || 'Data not available'}</p>
        </section>
      </div>

      <div className="mt-6">
        <button
          onClick={() => setShowSources(!showSources)}
          className="text-sm text-[var(--color-primary-400)] hover:underline flex items-center gap-1.5"
        >
          <Database size={14} /> {showSources ? 'Masquer' : 'Afficher'} la traçabilité des sources ({sources.length})
        </button>
        {showSources && (
          <div className="card-elevated mt-3 overflow-x-auto animate-fade-in">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-[var(--color-text-muted)] border-b border-[var(--color-border)]">
                  <th className="px-4 py-3">Champ</th>
                  <th className="px-4 py-3">Valeur</th>
                  <th className="px-4 py-3">Source</th>
                  <th className="px-4 py-3">Récupéré le</th>
                </tr>
              </thead>
              <tbody>
                {sources.map((s, i) => (
                  <tr key={i} className="border-b border-[var(--color-border-soft)] last:border-0">
                    <td className="px-4 py-2.5 text-[var(--color-text)]">{s.field_name}</td>
                    <td className="px-4 py-2.5 text-[var(--color-text-muted)]">{s.field_value ?? '—'}</td>
                    <td className="px-4 py-2.5">
                      <DataSourceBadge isDemoData={s.is_demo_data} />
                      <span className="ml-2 text-xs text-[var(--color-text-faint)]">{s.source_provider}</span>
                    </td>
                    <td className="px-4 py-2.5 text-xs text-[var(--color-text-faint)]">{formatDate(s.retrieved_at)}</td>
                  </tr>
                ))}
                {sources.length === 0 && (
                  <tr><td colSpan={4} className="px-4 py-6 text-center text-[var(--color-text-faint)]">Aucune source enregistrée pour ce véhicule.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="mt-8">
        <Link to="/search" className="text-sm text-[var(--color-text-muted)] hover:text-[var(--color-text)]">← Retour à la recherche</Link>
      </div>
    </div>
  )
}
