import type { ReactNode } from 'react'

type Variant = 'demo' | 'live' | 'neutral' | 'primary'

const classes: Record<Variant, string> = {
  demo: 'badge badge-demo',
  live: 'badge badge-live',
  neutral: 'badge badge-neutral',
  primary: 'badge badge-primary',
}

export function Badge({ variant = 'neutral', children, icon }: { variant?: Variant; children: ReactNode; icon?: ReactNode }) {
  return (
    <span className={classes[variant]}>
      {icon}
      {children}
    </span>
  )
}

export function DataSourceBadge({ isDemoData }: { isDemoData: boolean }) {
  return isDemoData ? <Badge variant="demo">Demo data</Badge> : <Badge variant="live">Live API</Badge>
}
