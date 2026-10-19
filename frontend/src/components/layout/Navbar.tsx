import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { Car, LayoutDashboard, LogOut, Menu, ScanLine, Search, User as UserIcon, X, GitCompare, History, Bookmark } from 'lucide-react'
import { useAuth } from '../../hooks/useAuth'

const navLinks = [
  { to: '/search', label: 'Recherche', icon: Search },
  { to: '/vin-decoder', label: 'VIN Decoder', icon: ScanLine },
  { to: '/compare', label: 'Comparer', icon: GitCompare },
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
]

export function Navbar() {
  const { isAuthenticated, user, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
      isActive
        ? 'bg-[var(--color-surface-3)] text-[var(--color-text)]'
        : 'text-[var(--color-text-muted)] hover:text-[var(--color-text)] hover:bg-[var(--color-surface-2)]'
    }`

  return (
    <header className="sticky top-0 z-40 border-b border-[var(--color-border)] bg-[var(--color-bg)]/85 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 shrink-0">
          <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-[var(--color-primary-500)] to-[var(--color-accent-500)] flex items-center justify-center">
            <Car size={17} className="text-white" />
          </div>
          <span className="font-display font-bold text-[var(--color-text)] hidden sm:block">VehicleIQ</span>
        </Link>

        <nav className="hidden md:flex items-center gap-1">
          {navLinks.map((link) => (
            <NavLink key={link.to} to={link.to} className={linkClass}>
              <link.icon size={15} /> {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="hidden md:flex items-center gap-2">
          {isAuthenticated ? (
            <div className="flex items-center gap-1">
              <NavLink to="/history" className={linkClass}><History size={15} /> Historique</NavLink>
              <NavLink to="/saved-comparisons" className={linkClass}><Bookmark size={15} /> Sauvegardes</NavLink>
              <NavLink to="/profile" className={linkClass}><UserIcon size={15} /> {user?.full_name.split(' ')[0]}</NavLink>
              <button
                onClick={() => { logout(); navigate('/') }}
                className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-[var(--color-danger-400)] hover:bg-[var(--color-surface-2)]"
              >
                <LogOut size={15} /> Déconnexion
              </button>
            </div>
          ) : (
            <>
              <Link to="/login" className="btn-secondary px-4 py-2 text-sm">Connexion</Link>
              <Link to="/register" className="btn-primary px-4 py-2 text-sm">Créer un compte</Link>
            </>
          )}
        </div>

        <button className="md:hidden text-[var(--color-text)]" onClick={() => setOpen(!open)}>
          {open ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {open && (
        <div className="md:hidden border-t border-[var(--color-border)] px-4 py-3 space-y-1 animate-fade-in">
          {navLinks.map((link) => (
            <NavLink key={link.to} to={link.to} className={linkClass} onClick={() => setOpen(false)}>
              <link.icon size={15} /> {link.label}
            </NavLink>
          ))}
          {isAuthenticated ? (
            <>
              <NavLink to="/history" className={linkClass} onClick={() => setOpen(false)}><History size={15} /> Historique</NavLink>
              <NavLink to="/saved-comparisons" className={linkClass} onClick={() => setOpen(false)}><Bookmark size={15} /> Sauvegardes</NavLink>
              <NavLink to="/profile" className={linkClass} onClick={() => setOpen(false)}><UserIcon size={15} /> Profil</NavLink>
              <button
                onClick={() => { logout(); setOpen(false); navigate('/') }}
                className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-[var(--color-danger-400)]"
              >
                <LogOut size={15} /> Déconnexion
              </button>
            </>
          ) : (
            <div className="flex gap-2 pt-2">
              <Link to="/login" className="btn-secondary flex-1 text-center px-4 py-2 text-sm" onClick={() => setOpen(false)}>Connexion</Link>
              <Link to="/register" className="btn-primary flex-1 text-center px-4 py-2 text-sm" onClick={() => setOpen(false)}>Créer un compte</Link>
            </div>
          )}
        </div>
      )}
    </header>
  )
}
