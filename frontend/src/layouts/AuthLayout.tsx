import { Link, Outlet } from 'react-router-dom'
import { Car } from 'lucide-react'

export function AuthLayout() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-grid px-4 py-12">
      <div className="w-full max-w-md">
        <Link to="/" className="flex items-center justify-center gap-2 mb-8">
          <div className="h-9 w-9 rounded-lg bg-gradient-to-br from-[var(--color-primary-500)] to-[var(--color-accent-500)] flex items-center justify-center">
            <Car size={18} className="text-white" />
          </div>
          <span className="font-display font-bold text-lg text-[var(--color-text)]">VehicleIQ</span>
        </Link>
        <div className="card-elevated p-6 sm:p-8 animate-fade-in">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
