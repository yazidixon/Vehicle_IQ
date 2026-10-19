import { Link } from 'react-router-dom'
import { ServerCrash } from 'lucide-react'

export function ServerErrorPage() {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
      <ServerCrash size={40} className="text-[var(--color-danger-400)] mb-4" />
      <h1 className="font-display text-5xl font-bold text-[var(--color-text)]">500</h1>
      <p className="text-[var(--color-text-muted)] mt-2 max-w-sm">
        Une erreur serveur est survenue. Vérifiez que le backend est démarré (localhost:8000).
      </p>
      <Link to="/" className="btn-primary mt-6 px-5 py-2.5 text-sm">Retour à l'accueil</Link>
    </div>
  )
}
