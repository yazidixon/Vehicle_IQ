export interface VinDecodeResult {
  vin: string
  is_valid: boolean
  country_of_manufacture: string
  manufacturer: string
  plant: string
  model_year: string
  make: string
  model: string
  series: string
  trim: string
  engine_code: string
  body_class: string
  source_provider: string
  is_demo_data: boolean
  warnings: string[]
}

export const NOT_AVAILABLE = 'Data not available'
