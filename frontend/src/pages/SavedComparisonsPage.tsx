import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Bookmark, Eye, Pencil, Trash2 } from 'lucide-react'
import { comparisonService } from '../services/comparisonService'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { SkeletonRow } from '../components/ui/Skeleton'
import { Modal } from '../components/ui/Modal'
import { useToast } from '../hooks/useToast'
import { extractApiError } from '../services/api'
import { formatDate } from '../utils/formatters'
import type { SavedComparison } from '../types/comparison'

export function SavedComparisonsPage() {
  const [comparisons, setComparisons] = useState<SavedComparison[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [renaming, setRenaming] = useState<SavedComparison | null>(null)
  const [newName, setNewName] = useState('')
  const navigate = useNavigate()
  const { showToast } = useToast()

  function load() {
    setLoading(true)
    setError(null)
    comparisonService.list().then(setComparisons).catch((err) => setError(extractApiError(err).message)).finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function handleDelete(id: string) {
    if (!window.confirm('Supprimer cette comparaison sauvegardée ?')) return
    try {
      await comparisonService.remove(id)
      setComparisons((prev) => prev?.filter((c) => c.id !== id) || null)
      showToast('Comparaison supprimée.', 'success')
    } catch (err) {
      showToast(extractApiError(err).message || 'Suppression impossible.', 'error')
    }
  }

  async function handleRename() {
    if (!renaming || !newName.trim()) return
    try {
      const updated = await comparisonService.rename(renaming.id, newName.trim())
      setComparisons((prev) => prev?.map((c) => (c.id === updated.id ? updated : c)) || null)
      showToast('Comparaison renommée.', 'success')
      setRenaming(null)
    } catch (err) {
      showToast(extractApiError(err).message || 'Renommage impossible.', 'error')
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8">
      <div className="mb-6 flex items-center gap-2">
        <Bookmark size={20} className="text-[var(--color-primary-400)]" />
        <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Comparaisons sauvegardées</h1>
      </div>

      {loading && <div className="card-elevated divide-y divide-[var(--color-border-soft)]">{Array.from({ length: 3 }).map((_, i) => <SkeletonRow key={i} />)}</div>}
      {!loading && error && <ErrorState message={error} onRetry={load} />}
      {!loading && !error && comparisons && comparisons.length === 0 && (
        <EmptyState icon={<Bookmark size={32} />} title="Aucune comparaison sauvegardée"
          description="Comparez des véhicules puis sauvegardez la comparaison pour la retrouver ici."
          action={<button onClick={() => navigate('/compare')} className="btn-primary px-4 py-2 text-sm">Comparer des véhicules</button>}
        />
      )}

      {!loading && !error && comparisons && comparisons.length > 0 && (
        <div className="card-elevated divide-y divide-[var(--color-border-soft)]">
          {comparisons.map((c) => (
            <div key={c.id} className="flex items-center gap-4 p-4">
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-[var(--color-text)]">{c.name}</p>
                <p className="text-xs text-[var(--color-text-muted)]">
                  {c.vehicle_ids.length} véhicule(s) — sauvegardé le {formatDate(c.created_at)}
                </p>
              </div>
              <div className="flex gap-1 shrink-0">
                <button onClick={() => navigate(`/compare?ids=${c.vehicle_ids.join(',')}`)} className="btn-secondary p-2" title="Voir">
                  <Eye size={14} />
                </button>
                <button onClick={() => { setRenaming(c); setNewName(c.name) }} className="btn-secondary p-2" title="Renommer">
                  <Pencil size={14} />
                </button>
                <button onClick={() => handleDelete(c.id)} className="btn-secondary p-2 text-[var(--color-danger-400)]" title="Supprimer">
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {renaming && (
        <Modal
          title="Renommer la comparaison"
          onClose={() => setRenaming(null)}
          footer={
            <>
              <button onClick={() => setRenaming(null)} className="btn-secondary px-4 py-2 text-sm">Annuler</button>
              <button onClick={handleRename} disabled={!newName.trim()} className="btn-primary px-4 py-2 text-sm">Enregistrer</button>
            </>
          }
        >
          <input value={newName} onChange={(e) => setNewName(e.target.value)} className="input-field w-full px-3 py-2.5 text-sm" autoFocus />
        </Modal>
      )}
    </div>
  )
}
