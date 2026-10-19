import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { ComparisonTable } from './ComparisonTable'
import type { ComparisonResult } from '../../types/comparison'
import type { Vehicle } from '../../types/vehicle'

function makeVehicle(id: string, make: string, model: string): Vehicle {
  return {
    id, manufacturer: make, make, model, generation: null, year: 2023, trim: null,
    body_type: null, fuel_type: 'Essence', engine_code: null, image_url: null,
    source_provider: 'demo', is_demo_data: true, specification: null,
  }
}

const result: ComparisonResult = {
  vehicles: [makeVehicle('v1', 'Weak', 'Model'), makeVehicle('v2', 'Strong', 'Model')],
  metrics: [
    {
      key: 'horsepower_hp', label: 'Puissance', unit: 'ch', better_direction: 'higher',
      values: { v1: 90, v2: 300 }, best_vehicle_id: 'v2',
    },
    {
      key: 'fuel_consumption_mixed_l100km', label: 'Consommation mixte', unit: 'L/100km', better_direction: 'lower',
      values: { v1: 5.0, v2: null }, best_vehicle_id: 'v1',
    },
  ],
}

describe('ComparisonTable', () => {
  it('renders every vehicle name as a column header', () => {
    render(<ComparisonTable result={result} />)
    expect(screen.getByText('Weak')).toBeInTheDocument()
    expect(screen.getByText('Strong')).toBeInTheDocument()
  })

  it('never renders a missing value as blank — it shows the sentinel text', () => {
    render(<ComparisonTable result={result} />)
    expect(screen.getByText('Data not available')).toBeInTheDocument()
  })

  it('renders the raw numeric values for present metrics', () => {
    render(<ComparisonTable result={result} />)
    expect(screen.getByText('90')).toBeInTheDocument()
    expect(screen.getByText('300')).toBeInTheDocument()
  })
})
