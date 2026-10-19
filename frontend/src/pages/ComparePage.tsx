import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { GitCompare, Save, X } from 'lucide-react'
import { VehiclePicker } from '../components/compare/VehiclePicker'
import { ComparisonTable } from '../components/compare/ComparisonTable'
import { RadarChartCard } from '../charts/RadarChartCard'
import { Modal } from '../components/ui/Modal'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { Skeleton } from '../components/ui/Skeleton'
import { comparisonService } from '../services/comparisonService'
import { extractApiError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import type { ComparisonResult } from '../types/comparison'
import type { VehicleSummary } from '../types/vehicle'

const RADAR_METRICS = ['horsepower_hp', 'torque_nm', 'top_speed_kmh', 'weight_kg', 'co2_emissions_gkm']

function normalize(values: number[], value: number | null, higherBetter: boolean): number {
  if (value === null || values.length === 0) return 0
  const max = Math.max(...values.filter((v) => v !== null))
  const min = Math.min(...values.filter((v) => v !== null))
  if (max === min) return 100
  const ratio = (value - min) / (max - min)
  return Math.round((higherBetter ? ratio : 1 - ratio) * 100)
}

export function ComparePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [selected, setSelected] = useState<VehicleSummary[]>([])
  const [result, setResult] = useState<ComparisonResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [saveModalOpen, setSaveModalOpen] = useState(false)
  const [comparisonName, setComparisonName] = useState('')
  const [saving, setSaving] = useState(false)
  const { isAuthenticated } = useAuth()
  const { showToast } = useToast()

  const ids = selected.map((v) => v.id)

  useEffect(() => {
    const preloadIds = searchParams.get('ids')
    if (preloadIds) {
      // Minimal shape; full vehicle data isn't needed until compute() returns full VehicleResponse.
      const stub = preloadIds.split(',').map((id) => ({ id } as VehicleSummary))
      setSelected(stub)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (ids.length < 2) {
      setResult(null)
      return
    }
    setLoading(true)
    setError(null)
    comparisonService.compute(ids)
      .then(setResult)
      .catch((err) => setError(extractApiError(err).message))
      .finally(() => setLoading(false))
  }, [JSON.stringify(ids)])

  function removeVehicle(id: string) {
    setSelected((prev) => prev.filter((v) => v.id !== id))
  }

  function addVehicle(v: VehicleSummary) {
    setSelected((prev) => (prev.some((s) => s.id === v.id) ? prev : [...prev, v]))
    setSearchParams({})
  }

  async function handleSave() {
    if (!comparisonName.trim()) return
    setSaving(true)
    try {
      await comparisonService.save(comparisonName.trim(), ids)
      showToast('Comparaison sauvegardée.', 'success')
      setSaveModalOpen(false)
      setComparisonName('')
    } catch (err) {
      showToast(extractApiError(err).message || 'Sauvegarde impossible.', 'error')
    } finally {
      setSaving(false)
    }
  }

  const radarData = result
    ? RADAR_METRICS.map((key) => {
        const metric = result.metrics.find((m) => m.key === key)
        if (!metric) return null
        const rawValues = result.vehicles.map((v) => {
          const val = metric.values[v.id]
          return typeof val === 'number' ? val : null
        })
        const numeric = rawValues.filter((v): v is number => v !== null)
        const row: Record<string, string | number> = { metric: metric.label }
        result.vehicles.forEach((v, i) => {
          row[`${v.make} ${v.model}`] = normalize(numeric, rawValues[i], metric.better_direction !== 'lower')
        })
        return row
      }).filter((r): r is Record<string, string | number> => r !== null)
    : []

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
      <div className="mb-6 flex items-center gap-2">
        <GitCompare size={20} className="text-[var(--color-primary-400)]" />
        <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Comparer des véhicules</h1>
      </div>

      <div className="mb-4">
        <VehiclePicker selectedIds={ids} onAdd={addVehicle} />
      </div>

      {selected.length > 0 && (
        <div className="flex flex-wrap gap-2 mb-6">
          {selected.map((v) => (
            <span key={v.id} className="badge badge-primary pr-1.5">
              {v.make ? `${v.make} ${v.model}` : v.id.slice(0, 8)}
              <button onClick={() => removeVehicle(v.id)} className="ml-1 hover:text-white">
                <X size={11} />
              </button>
            </span>
          ))}
        </div>
      )}

      {selected.length < 2 && (
        <EmptyState
          icon={<GitCompare size={32} />}
          title="Sélectionnez au moins 2 véhicules"
          description="Utilisez la barre de recherche ci-dessus pour ajouter jusqu'à 4 véhicules à comparer."
        />
      )}

      {loading && <Skeleton className="h-96 w-full" />}
      {!loading && error && <ErrorState message={error} />}

      {!loading && !error && result && (
        <div className="space-y-6 animate-fade-in">
          <div className="flex justify-end">
            {isAuthenticated && (
              <button onClick={() => setSaveModalOpen(true)} className="btn-secondary px-4 py-2 text-sm flex items-center gap-2">
                <Save size={14} /> Sauvegarder cette comparaison
              </button>
            )}
          </div>
          <ComparisonTable result={result} />
          <RadarChartCard
            title="Vue synthétique (score normalisé 0-100, plus haut = meilleur)"
            data={radarData}
            series={result.vehicles.map((v) => ({ name: `${v.make} ${v.model}` }))}
          />
        </div>
      )}

      {saveModalOpen && (
        <Modal
          title="Sauvegarder la comparaison"
          onClose={() => setSaveModalOpen(false)}
          footer={
            <>
              <button onClick={() => setSaveModalOpen(false)} className="btn-secondary px-4 py-2 text-sm">Annuler</button>
              <button onClick={handleSave} disabled={saving || !comparisonName.trim()} className="btn-primary px-4 py-2 text-sm">
                Sauvegarder
              </button>
            </>
          }
        >
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Nom de la comparaison</label>
          <input
            value={comparisonName}
            onChange={(e) => setComparisonName(e.target.value)}
            placeholder="Ex : Berlines familiales 2024"
            className="input-field w-full px-3 py-2.5 text-sm"
            autoFocus
          />
        </Modal>
      )}
    </div>
  )
}
