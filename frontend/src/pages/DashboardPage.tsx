import { useEffect, useState } from 'react'
import { BarChart3, Bookmark, ScanLine, Search as SearchIcon, Car } from 'lucide-react'
import { dashboardService } from '../services/historyService'
import { StatCard } from '../components/ui/StatCard'
import { BarChartCard } from '../charts/BarChartCard'
import { LineChartCard } from '../charts/LineChartCard'
import { SkeletonGrid } from '../components/ui/Skeleton'
import { ErrorState } from '../components/ui/ErrorState'
import { extractApiError } from '../services/api'
import type { DashboardResponse } from '../types/history'

export function DashboardPage() {
  const [data, setData] = useState<DashboardResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  function load() {
    setLoading(true)
    setError(null)
    dashboardService.get()
      .then(setData)
      .catch((err) => setError(extractApiError(err).message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      <div className="mb-6 flex items-center gap-2">
        <BarChart3 size={20} className="text-[var(--color-primary-400)]" />
        <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Dashboard analytique</h1>
      </div>

      {loading && <SkeletonGrid count={4} />}
      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && data && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <StatCard label="Recherches effectuées" value={data.stats.total_searches} icon={SearchIcon} accent="primary" />
            <StatCard label="VIN décodés" value={data.stats.total_vin_decodes} icon={ScanLine} accent="accent" />
            <StatCard label="Véhicules consultés" value={data.stats.total_vehicles_viewed} icon={Car} accent="amber" />
            <StatCard label="Comparaisons sauvegardées" value={data.stats.total_comparisons_saved} icon={Bookmark} accent="primary" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 mb-5">
            <BarChartCard title="Puissance moyenne par marque" data={data.average_horsepower_by_make} unit="ch" color="var(--color-primary-500)" />
            <BarChartCard title="Émissions CO₂ moyennes par marque" data={data.average_co2_by_make} unit="g/km" color="var(--color-amber-500)" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            <BarChartCard title="Recherches par carburant" data={data.searches_by_fuel_type} color="var(--color-accent-500)" />
            <LineChartCard title="Activité de recherche dans le temps" data={data.searches_over_time} />
          </div>
        </>
      )}
    </div>
  )
}
