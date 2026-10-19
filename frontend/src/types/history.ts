export interface SearchHistoryEntry {
  id: string
  search_type: 'filter' | 'vin' | 'vehicle_view'
  query_params: Record<string, unknown> | null
  result_summary: string | null
  created_at: string
}

export interface DashboardStats {
  total_searches: number
  total_vin_decodes: number
  total_vehicles_viewed: number
  total_comparisons_saved: number
}

export interface DashboardDistributionItem {
  label: string
  value: number
}

export interface DashboardResponse {
  stats: DashboardStats
  searches_by_fuel_type: DashboardDistributionItem[]
  searches_over_time: DashboardDistributionItem[]
  average_horsepower_by_make: DashboardDistributionItem[]
  average_co2_by_make: DashboardDistributionItem[]
}
