import { NOT_AVAILABLE } from '../../utils/formatters'

export function SpecRow({ label, value, unit }: { label: string; value: string | number | null | undefined; unit?: string }) {
  const isMissing = value === null || value === undefined || value === ''
  return (
    <div className="flex items-center justify-between py-2.5 border-b border-[var(--color-border-soft)] last:border-0">
      <span className="text-sm text-[var(--color-text-muted)]">{label}</span>
      <span className={`text-sm font-medium ${isMissing ? 'text-[var(--color-text-faint)] italic' : 'text-[var(--color-text)]'}`}>
        {isMissing ? NOT_AVAILABLE : unit ? `${value} ${unit}` : value}
      </span>
    </div>
  )
}
