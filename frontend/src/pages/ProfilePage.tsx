import { useState, type FormEvent } from 'react'
import { Loader2, Save, UserCircle } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { authService } from '../services/authService'
import { extractApiError } from '../services/api'
import { formatDateShort } from '../utils/formatters'

export function ProfilePage() {
  const { user, refreshProfile } = useAuth()
  const { showToast } = useToast()
  const [fullName, setFullName] = useState(user?.full_name || '')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      await authService.updateProfile({ full_name: fullName || undefined, password: password || undefined })
      await refreshProfile()
      setPassword('')
      showToast('Profil mis à jour.', 'success')
    } catch (err) {
      showToast(extractApiError(err).message || 'Mise à jour impossible.', 'error')
    } finally {
      setLoading(false)
    }
  }

  if (!user) return null

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 py-10">
      <div className="flex items-center gap-4 mb-8">
        <div className="h-16 w-16 rounded-full bg-[var(--color-surface-3)] flex items-center justify-center">
          <UserCircle size={36} className="text-[var(--color-text-muted)]" />
        </div>
        <div>
          <h1 className="font-display text-xl font-bold text-[var(--color-text)]">{user.full_name}</h1>
          <p className="text-sm text-[var(--color-text-muted)]">{user.email}</p>
          <p className="text-xs text-[var(--color-text-faint)] mt-0.5">Membre depuis {formatDateShort(user.created_at)}</p>
        </div>
      </div>

      <div className="card-elevated p-6">
        <h2 className="font-display font-semibold text-[var(--color-text)] mb-4">Modifier le profil</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Nom complet</label>
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} className="input-field w-full px-3 py-2.5 text-sm" />
          </div>
          <div>
            <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Nouveau mot de passe (optionnel)</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
              placeholder="Laisser vide pour ne pas changer" className="input-field w-full px-3 py-2.5 text-sm" />
          </div>
          <button type="submit" disabled={loading} className="btn-primary px-5 py-2.5 text-sm flex items-center gap-2">
            {loading ? <Loader2 size={16} className="animate-spin" /> : <Save size={16} />}
            Enregistrer
          </button>
        </form>
      </div>
    </div>
  )
}
