export interface VehicleSpecification {
  engine_type: string | null
  engine_displacement_cc: number | null
  horsepower_hp: number | null
  torque_nm: number | null
  transmission: string | null
  drivetrain: string | null

  top_speed_kmh: number | null
  acceleration_0_100_s: number | null

  length_mm: number | null
  width_mm: number | null
  height_mm: number | null
  wheelbase_mm: number | null
  weight_kg: number | null
  doors: number | null
  seats: number | null

  fuel_consumption_urban_l100km: number | null
  fuel_consumption_extra_urban_l100km: number | null
  fuel_consumption_mixed_l100km: number | null
  co2_emissions_gkm: number | null
  euro_norm: string | null
  energy_class: string | null

  price_msrp: number | null
  currency: string | null

  equipment: string[] | null
  safety_notes: string | null

  is_demo_data: boolean
}

export interface Vehicle {
  id: string
  manufacturer: string
  make: string
  model: string
  generation: string | null
  year: number
  trim: string | null
  body_type: string | null
  fuel_type: string | null
  engine_code: string | null
  image_url: string | null
  source_provider: string
  is_demo_data: boolean
  specification: VehicleSpecification | null
}

export interface VehicleSummary {
  id: string
  manufacturer: string
  make: string
  model: string
  year: number
  trim: string | null
  fuel_type: string | null
  horsepower_hp: number | null
  transmission: string | null
  image_url: string | null
  is_demo_data: boolean
}

export interface VehicleSourceData {
  field_name: string
  field_value: string | null
  source_provider: string
  source_endpoint: string | null
  is_demo_data: boolean
  retrieved_at: string
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  page_size: number
  total_pages: number
}

export interface VehicleSearchFilters {
  q?: string
  make?: string
  model?: string
  manufacturer?: string
  year?: number
  year_min?: number
  year_max?: number
  fuel_type?: string
  engine_code?: string
  page?: number
  page_size?: number
}
