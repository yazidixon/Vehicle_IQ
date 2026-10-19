import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LogIn, Loader2 } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { extractApiError } from '../services/api'

export function LoginPage() {
  const { login } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const from = (location.state as { from?: Location })?.from?.pathname || '/dashboard'

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login({ email, password })
      showToast('Connexion réussie. Bon retour !', 'success')
      navigate(from, { replace: true })
    } catch (err) {
      const apiError = extractApiError(err)
      setError(apiError.message || 'Connexion impossible.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Connexion</h1>
      <p className="text-sm text-[var(--color-text-muted)] mt-1 mb-6">Accédez à votre historique et vos comparaisons.</p>

      {error && (
        <div className="mb-4 text-sm text-[var(--color-danger-400)] bg-[var(--color-danger-500)]/10 border border-[var(--color-danger-500)]/30 rounded-lg px-3 py-2">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Email</label>
          <input
            type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
            className="input-field w-full px-3 py-2.5 text-sm" placeholder="vous@exemple.com"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-[var(--color-text-muted)] mb-1.5">Mot de passe</label>
          <input
            type="password" required value={password} onChange={(e) => setPassword(e.target.value)}
            className="input-field w-full px-3 py-2.5 text-sm" placeholder="••••••••"
          />
        </div>
        <button type="submit" disabled={loading} className="btn-primary w-full py-2.5 text-sm flex items-center justify-center gap-2">
          {loading ? <Loader2 size={16} className="animate-spin" /> : <LogIn size={16} />}
          Se connecter
        </button>
      </form>

      <p className="text-sm text-[var(--color-text-muted)] mt-6 text-center">
        Pas encore de compte ? <Link to="/register" className="text-[var(--color-primary-400)] hover:underline">Créer un compte</Link>
      </p>
    </div>
  )
}
