import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { DashboardDistributionItem } from '../types/history'

export function LineChartCard({ title, data }: { title: string; data: DashboardDistributionItem[] }) {
  return (
    <div className="card-elevated p-5">
      <h3 className="font-display font-semibold text-sm text-[var(--color-text)] mb-4">{title}</h3>
      {data.length === 0 ? (
        <p className="text-sm text-[var(--color-text-faint)] italic py-8 text-center">Pas encore de données.</p>
      ) : (
        <ResponsiveContainer width="100%" height={220}>
          <LineChart data={data} margin={{ top: 4, right: 8, left: -16, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" vertical={false} />
            <XAxis dataKey="label" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
            <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} allowDecimals={false} />
            <Tooltip contentStyle={{ background: 'var(--color-surface-2)', border: '1px solid var(--color-border)', borderRadius: 8, fontSize: 12 }} />
            <Line type="monotone" dataKey="value" stroke="var(--color-accent-500)" strokeWidth={2} dot={{ r: 3 }} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}
