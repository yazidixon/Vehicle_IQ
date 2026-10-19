import { useEffect, useState } from 'react'
import { History, ScanLine, Search as SearchIcon, Car } from 'lucide-react'
import { historyService } from '../services/historyService'
import { SkeletonRow } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { extractApiError } from '../services/api'
import { formatDate } from '../utils/formatters'
import type { SearchHistoryEntry } from '../types/history'

const TYPE_META: Record<string, { icon: typeof SearchIcon; label: string }> = {
  filter: { icon: SearchIcon, label: 'Recherche filtrée' },
  vin: { icon: ScanLine, label: 'Décodage VIN' },
  vehicle_view: { icon: Car, label: 'Consultation véhicule' },
}

export function HistoryPage() {
  const [entries, setEntries] = useState<SearchHistoryEntry[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  function load() {
    setLoading(true)
    setError(null)
    historyService.list().then(setEntries).catch((err) => setError(extractApiError(err).message)).finally(() => setLoading(false))
  }

  useEffect(load, [])

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8">
      <div className="mb-6 flex items-center gap-2">
        <History size={20} className="text-[var(--color-primary-400)]" />
        <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Historique</h1>
      </div>

      {loading && <div className="card-elevated divide-y divide-[var(--color-border-soft)]">{Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} />)}</div>}
      {!loading && error && <ErrorState message={error} onRetry={load} />}
      {!loading && !error && entries && entries.length === 0 && (
        <EmptyState icon={<History size={32} />} title="Aucun historique" description="Vos recherches, décodages VIN et consultations de véhicules apparaîtront ici." />
      )}

      {!loading && !error && entries && entries.length > 0 && (
        <div className="card-elevated divide-y divide-[var(--color-border-soft)]">
          {entries.map((entry) => {
            const meta = TYPE_META[entry.search_type] || TYPE_META.filter
            const Icon = meta.icon
            return (
              <div key={entry.id} className="flex items-center gap-4 p-4">
                <div className="h-9 w-9 rounded-lg bg-[var(--color-primary-500)]/10 flex items-center justify-center text-[var(--color-primary-400)] shrink-0">
                  <Icon size={16} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-[var(--color-text)]">{meta.label}</p>
                  <p className="text-xs text-[var(--color-text-muted)] truncate">{entry.result_summary || '—'}</p>
                </div>
                <span className="text-xs text-[var(--color-text-faint)] shrink-0">{formatDate(entry.created_at)}</span>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
