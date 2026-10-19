import { Link } from 'react-router-dom'
import { Car } from 'lucide-react'

export function Footer() {
  return (
    <footer className="border-t border-[var(--color-border)] mt-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-10 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8">
        <div>
          <div className="flex items-center gap-2">
            <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-[var(--color-primary-500)] to-[var(--color-accent-500)] flex items-center justify-center">
              <Car size={14} className="text-white" />
            </div>
            <span className="font-display font-bold text-[var(--color-text)]">VehicleIQ</span>
          </div>
        </div>

        <div>
          <h4 className="text-xs font-semibold text-[var(--color-text)] uppercase tracking-wide mb-3">Fonctionnalités</h4>
          <ul className="space-y-2 text-sm text-[var(--color-text-muted)]">
            <li><Link to="/search" className="hover:text-[var(--color-text)]">Recherche de véhicules</Link></li>
            <li><Link to="/vin-decoder" className="hover:text-[var(--color-text)]">Décodeur VIN</Link></li>
            <li><Link to="/compare" className="hover:text-[var(--color-text)]">Comparaison</Link></li>
            <li><Link to="/dashboard" className="hover:text-[var(--color-text)]">Dashboard analytique</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-semibold text-[var(--color-text)] uppercase tracking-wide mb-3">Sources de données</h4>
          <ul className="space-y-2 text-sm text-[var(--color-text-muted)]">
            <li>NHTSA vPIC (API publique, décodage VIN)</li>
            <li>Jeu de données local (spécifications, mode démo)</li>
          </ul>
        </div>
      </div>
      <div className="border-t border-[var(--color-border)] py-4 text-center text-xs text-[var(--color-text-faint)]">
        © {new Date().getFullYear()} VehicleIQ — usage local uniquement.
      </div>
    </footer>
  )
}
