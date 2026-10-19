import type { LucideIcon } from 'lucide-react'

export function StatCard({
  label,
  value,
  icon: Icon,
  accent = 'primary',
}: {
  label: string
  value: string | number
  icon: LucideIcon
  accent?: 'primary' | 'accent' | 'amber'
}) {
  const accentClasses = {
    primary: 'text-[var(--color-primary-400)] bg-[var(--color-primary-500)]/10',
    accent: 'text-[var(--color-accent-400)] bg-[var(--color-accent-500)]/10',
    amber: 'text-[var(--color-amber-400)] bg-[var(--color-amber-500)]/10',
  }[accent]

  return (
    <div className="card-elevated p-5 flex items-center gap-4 card-hover">
      <div className={`h-11 w-11 rounded-xl flex items-center justify-center ${accentClasses}`}>
        <Icon size={20} />
      </div>
      <div>
        <p className="text-2xl font-display font-bold text-[var(--color-text)] leading-tight">{value}</p>
        <p className="text-xs text-[var(--color-text-muted)] mt-0.5">{label}</p>
      </div>
    </div>
  )
}
