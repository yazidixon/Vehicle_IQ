import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { UserPlus, Loader2 } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { extractApiError } from '../services/api'

export function RegisterPage() {
  const { register } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (password.length < 8) {
      setError('Le mot de passe doit contenir au moins 8 caractères.')
      return
    }
    setLoading(true)
    try {
      await register({ email, full_name: fullName, password })
      showToast('Compte créé avec succès. Bienvenue !', 'success')
      navigate('/dashboard', { replace: true })
    } catch (err) {
      const apiError = extractApiError(err)
      setError(apiError.message || 'Impossible de créer le compte.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Créer un compte</h1>
      <p className="text-sm text-[var(--color-text-muted)] mt-1 mb-6">Sauvegardez vos recherches et vos comparaisons.</p>

      {error && (
        <div className="mb-4 text-sm text-[var(--color-danger-400)] bg-[var(--color-danger-500)]/10 border border-[var(--color-danger-500)]/30 rounded-lg px-3 py-2">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Nom complet</label>
          <input required value={fullName} onChange={(e) => setFullName(e.target.value)}
            className="input-field w-full px-3 py-2.5 text-sm" placeholder="Jean Dupont" />
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Email</label>
          <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
            className="input-field w-full px-3 py-2.5 text-sm" placeholder="vous@exemple.com" />
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Mot de passe (8 caractères min.)</label>
          <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)}
            className="input-field w-full px-3 py-2.5 text-sm" placeholder="••••••••" />
        </div>
        <button type="submit" disabled={loading} className="btn-primary w-full py-2.5 text-sm flex items-center justify-center gap-2">
          {loading ? <Loader2 size={16} className="animate-spin" /> : <UserPlus size={16} />}
          Créer mon compte
        </button>
      </form>

      <p className="text-sm text-[var(--color-text-muted)] mt-6 text-center">
        Déjà un compte ? <Link to="/login" className="text-[var(--color-primary-400)] hover:underline">Se connecter</Link>
      </p>
    </div>
  )
}
