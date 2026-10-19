import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { DashboardDistributionItem } from '../types/history'

export function BarChartCard({
  title,
  data,
  color = 'var(--color-primary-500)',
  unit,
}: {
  title: string
  data: DashboardDistributionItem[]
  color?: string
  unit?: string
}) {
  return (
    <div className="card-elevated p-5">
      <h3 className="font-display font-semibold text-sm text-[var(--color-text)] mb-4">{title}</h3>
      {data.length === 0 ? (
        <p className="text-sm text-[var(--color-text-faint)] italic py-8 text-center">Pas encore de données.</p>
      ) : (
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={data} margin={{ top: 4, right: 8, left: -16, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" vertical={false} />
            <XAxis dataKey="label" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} interval={0} angle={-35} textAnchor="end" height={60} />
            <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
            <Tooltip
              contentStyle={{ background: 'var(--color-surface-2)', border: '1px solid var(--color-border)', borderRadius: 8, fontSize: 12 }}
              labelStyle={{ color: 'var(--color-text)' }}
              formatter={(value) => [unit ? `${value} ${unit}` : String(value), '']}
            />
            <Bar dataKey="value" fill={color} radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}
