import type { ReactNode } from 'react'

export function EmptyState({
  icon,
  title,
  description,
  action,
}: {
  icon?: ReactNode
  title: string
  description?: string
  action?: ReactNode
}) {
  return (
    <div className="flex flex-col items-center justify-center text-center py-16 px-6 card border-dashed">
      {icon && <div className="mb-4 text-[var(--color-text-faint)]">{icon}</div>}
      <h3 className="font-display text-lg font-semibold text-[var(--color-text)]">{title}</h3>
      {description && <p className="mt-1.5 text-sm text-[var(--color-text-muted)] max-w-sm">{description}</p>}
      {action && <div className="mt-5">{action}</div>}
    </div>
  )
}
