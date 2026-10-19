import { Link } from 'react-router-dom'
import { Compass } from 'lucide-react'

export function NotFoundPage() {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
      <Compass size={40} className="text-[var(--color-primary-400)] mb-4" />
      <h1 className="font-display text-5xl font-bold text-[var(--color-text)]">404</h1>
      <p className="text-[var(--color-text-muted)] mt-2 max-w-sm">
        Cette page n'existe pas ou a été déplacée.
      </p>
      <Link to="/" className="btn-primary mt-6 px-5 py-2.5 text-sm">Retour à l'accueil</Link>
    </div>
  )
}
