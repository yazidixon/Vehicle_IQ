import type { Vehicle } from './vehicle'

export interface ComparisonMetricRow {
  key: string
  label: string
  unit: string | null
  better_direction: 'higher' | 'lower' | 'neutral'
  values: Record<string, number | string | null>
  best_vehicle_id: string | null
}

export interface ComparisonResult {
  vehicles: Vehicle[]
  metrics: ComparisonMetricRow[]
}

export interface SavedComparison {
  id: string
  name: string
  vehicle_ids: string[]
  created_at: string
  updated_at: string
}
