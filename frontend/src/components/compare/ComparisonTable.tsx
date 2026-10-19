import { Trophy } from 'lucide-react'
import type { ComparisonResult } from '../../types/comparison'
import { NOT_AVAILABLE } from '../../utils/formatters'

export function ComparisonTable({ result }: { result: ComparisonResult }) {
  return (
    <div className="card-elevated overflow-x-auto">
      <table className="w-full text-sm min-w-[600px]">
        <thead>
          <tr className="border-b border-[var(--color-border)]">
            <th className="text-left px-4 py-3 text-xs text-[var(--color-text-muted)] sticky left-0 bg-[var(--color-surface)]">Critère</th>
            {result.vehicles.map((v) => (
              <th key={v.id} className="text-left px-4 py-3">
                <p className="text-xs text-[var(--color-text-muted)]">{v.manufacturer}</p>
                <p className="font-display font-semibold text-[var(--color-text)]">{v.make} {v.model}</p>
                <p className="text-xs text-[var(--color-text-faint)]">{v.year} {v.trim}</p>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {result.metrics.map((metric) => (
            <tr key={metric.key} className="border-b border-[var(--color-border-soft)] last:border-0">
              <td className="px-4 py-3 text-[var(--color-text-muted)] sticky left-0 bg-[var(--color-surface)]">
                {metric.label} {metric.unit && <span className="text-[var(--color-text-faint)]">({metric.unit})</span>}
              </td>
              {result.vehicles.map((v) => {
                const value = metric.values[v.id]
                const isBest = metric.best_vehicle_id === v.id
                const isMissing = value === null || value === undefined
                return (
                  <td key={v.id} className={`px-4 py-3 ${isBest ? 'bg-[var(--color-success-500)]/10' : ''}`}>
                    <span className={`inline-flex items-center gap-1.5 font-medium ${isMissing ? 'text-[var(--color-text-faint)] italic font-normal' : isBest ? 'text-[var(--color-success-400)]' : 'text-[var(--color-text)]'}`}>
                      {isBest && <Trophy size={12} />}
                      {isMissing ? NOT_AVAILABLE : String(value)}
                    </span>
                  </td>
                )
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
