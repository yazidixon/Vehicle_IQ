import {
  PolarAngleAxis, PolarGrid, Radar, RadarChart, ResponsiveContainer, Legend, Tooltip,
} from 'recharts'

const COLORS = ['var(--color-primary-500)', 'var(--color-accent-500)', 'var(--color-amber-500)', 'var(--color-danger-500)']

export interface RadarSeries {
  name: string
  color?: string
}

export function RadarChartCard({
  title,
  data,
  series,
}: {
  title: string
  data: Record<string, string | number>[]
  series: RadarSeries[]
}) {
  return (
    <div className="card-elevated p-5">
      <h3 className="font-display font-semibold text-sm text-[var(--color-text)] mb-4">{title}</h3>
      <ResponsiveContainer width="100%" height={320}>
        <RadarChart data={data} outerRadius="70%">
          <PolarGrid stroke="var(--color-border)" />
          <PolarAngleAxis dataKey="metric" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
          {series.map((s, i) => (
            <Radar
              key={s.name}
              name={s.name}
              dataKey={s.name}
              stroke={s.color || COLORS[i % COLORS.length]}
              fill={s.color || COLORS[i % COLORS.length]}
              fillOpacity={0.15}
              strokeWidth={2}
            />
          ))}
          <Legend wrapperStyle={{ fontSize: 12, color: 'var(--color-text-muted)' }} />
          <Tooltip contentStyle={{ background: 'var(--color-surface-2)', border: '1px solid var(--color-border)', borderRadius: 8, fontSize: 12 }} />
        </RadarChart>
      </ResponsiveContainer>
    </div>
  )
}
